package io.arex.inst.runtime.match.key;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.DatabaseUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class DatabaseMatchKeyBuilderImplTest {

    static DatabaseMatchKeyBuilderImpl instance;
    static ArexMocker mocker;

    @BeforeAll
    static void setUp() {
        instance = new DatabaseMatchKeyBuilderImpl();
        mocker = new ArexMocker(MockCategoryType.DYNAMIC_CLASS);
        mocker.setOperationName("database@table@select@query");
        mocker.setTargetRequest(new Mocker.Target());
        mocker.getTargetRequest().setAttribute(ArexConstants.DB_PARAMETERS, "id=1");
        mocker.getTargetRequest().setBody("select * from table where id=?");
        mocker.setTargetResponse(new Mocker.Target());
        Mockito.mockStatic(DatabaseUtils.class);
        Mockito.mockStatic(Serializer.class);
    }

    @AfterAll
    static void tearDown() {
        instance = null;
        mocker = null;
        Mockito.clearAllCaches();
    }

    @Test
    void isSupported() {
        assertTrue(instance.isSupported(MockCategoryType.DATABASE));
    }

    @Test
    void getFuzzyMatchKey() {
        Mockito.when(DatabaseUtils.parseDbName(any(), any())).thenReturn("database");
        assertNotEquals(instance.getFuzzyMatchKey(mocker), 0);
        Mockito.when(DatabaseUtils.parseTableNames(any())).thenReturn(Collections.singletonList("table"));
        assertNotEquals(instance.getFuzzyMatchKey(mocker), 0);
    }

    @Test
    void getAccurateMatchKey() {
        assertNotEquals(instance.getAccurateMatchKey(mocker), 0);
    }

    @Test
    void getEigenBody() {
        assertNull(instance.getEigenBody(mocker));
    }
}