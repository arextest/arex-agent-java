package io.arex.inst.runtime.service;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.inst.runtime.config.Config;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class DataServiceTest {

    static DataService dataService;

    @BeforeAll
    static void setUp() {
        dataService = new DataService(Mockito.mock(DataCollector.class));
        Mockito.mockStatic(Config.class);
        Config config = Mockito.mock(Config.class);
        Mockito.when(Config.get()).thenReturn(config);
        Mockito.when(config.isLocalStorage()).thenReturn(true);
    }

    @AfterAll
    static void tearDown() {
        dataService = null;
        Mockito.clearAllCaches();
    }

    @Test
    void save() {
        assertDoesNotThrow(() -> dataService.save(Collections.singletonList(new ArexMocker())));
    }

    @Test
    void invalidCase() {
        assertDoesNotThrow(() -> dataService.invalidCase("mock"));
    }

    @Test
    void query() {
        assertNull(dataService.query("mock", MockStrategyEnum.OVER_BREAK));
    }

    @Test
    void queryAll() {
        assertNull(dataService.queryAll("mock"));
    }

    @Test
    void setDataCollector() {
        DataCollector dataCollector = Mockito.mock(DataCollector.class);
        Mockito.when(dataCollector.order()).thenReturn(1);
        DataService.setDataCollector(Collections.singletonList(dataCollector));
        assertNotNull(DataService.INSTANCE);
    }
}