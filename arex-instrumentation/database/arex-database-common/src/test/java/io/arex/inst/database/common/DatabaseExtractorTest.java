package io.arex.inst.database.common;

import io.arex.foundation.model.DatabaseMocker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DatabaseExtractorTest {
    @InjectMocks
    DatabaseExtractor target;
    DatabaseMocker databaseMocker;

    @Test
    void record() {
        try (MockedConstruction<DatabaseMocker> mocked = Mockito.mockConstruction(DatabaseMocker.class, (mock, context) -> {
            databaseMocker = mock;
        })) {
            target.record(new Object());
            verify(databaseMocker).record();
        }
    }

    @Test
    void testRecord() {
        try (MockedConstruction<DatabaseMocker> mocked = Mockito.mockConstruction(DatabaseMocker.class, (mock, context) -> {
            databaseMocker = mock;
        })) {
            target.record(new SQLException());
            verify(databaseMocker).record();
        }
    }

    @Test
    void replayException() {
        try (MockedConstruction<DatabaseMocker> mocked = Mockito.mockConstruction(DatabaseMocker.class, (mock, context) -> {
            Mockito.when(mock.getExceptionMessage()).thenReturn("exception");
        })) {
            assertThrows(SQLException.class, target::replay);
        }
    }

    @Test
    void replay() throws SQLException {
        try (MockedConstruction<DatabaseMocker> mocked = Mockito.mockConstruction(DatabaseMocker.class)) {
            assertNotNull(target.replay());
        }
    }
}