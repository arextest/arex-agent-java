package io.arex.inst.database.common;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(MockitoExtension.class)
class DatabaseHelperTest {

    @ParameterizedTest
    @MethodSource("processCase")
    void getUrlFromDataSource(DataSource dataSource, Predicate<String> predicate) {
        DatabaseHelper.getUrlFromDataSource(dataSource);
    }

    static Stream<Arguments> processCase() {
        UnpooledDataSource dataSource1 = Mockito.mock(UnpooledDataSource.class);
        dataSource1.setUrl("test.url");
        PooledDataSource dataSource2 = Mockito.mock(PooledDataSource.class);
        dataSource1.setUrl("test.url");
        DataSource dataSource3 = Mockito.mock(DataSource.class);

        Predicate<String> predicate1 = Objects::isNull;
        Predicate<String> predicate2 = "test.url"::equals;

        return Stream.of(
                arguments(null, predicate1),
                arguments(dataSource1, predicate2),
                arguments(dataSource2, predicate2),
                arguments(dataSource3, predicate1)
        );
    }
}