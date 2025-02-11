package io.arex.inst.spring;

import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.config.ConfigBuilder;
import io.arex.inst.runtime.model.ArexConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SpringUtilTest {
    @BeforeEach
    void setUp() {
        ConfigBuilder test = ConfigBuilder.create("test");
        test.build();
    }

    @AfterEach
    void tearDown() {
        System.clearProperty(ArexConstants.SPRING_SCAN_PACKAGES);
        Mockito.clearAllCaches();
    }

    @Order(1)
    @Test
    void updateScanBasePackagesDoesNotUpdateConfigWhenNoBasePackages() {
        ConfigurableApplicationContext context = Mockito.mock(ConfigurableApplicationContext.class);
        Map<String, Object> scanAnnotation = Collections.singletonMap("bean1", new BeanWithoutComponentScan());
        Mockito.when(context.getBeansWithAnnotation(ComponentScan.class)).thenReturn(scanAnnotation);
        SpringUtil.updateScanBasePackages(context);
        assertTrue(CollectionUtil.isEmpty(Config.get().getCoveragePackages()));
    }

    @Order(2)
    @Test
    void updateScanBasePackagesHandlesEmptyContext() {
        ConfigurableApplicationContext context = Mockito.mock(ConfigurableApplicationContext.class);
        Mockito.when(context.getBeansWithAnnotation(ComponentScan.class)).thenReturn(Collections.EMPTY_MAP);
        SpringUtil.updateScanBasePackages(context);
        assertTrue(CollectionUtil.isEmpty(Config.get().getCoveragePackages()));
    }

    @Order(3)
    @Test
    void updateScanBasePackagesHandlesNullBasePackages() {
        ConfigurableApplicationContext context = Mockito.mock(ConfigurableApplicationContext.class);
        Map<String, Object> scanAnnotation = Collections.singletonMap("bean1", new BeanWithNullBasePackages());
        Mockito.when(context.getBeansWithAnnotation(ComponentScan.class)).thenReturn(scanAnnotation);
        SpringUtil.updateScanBasePackages(context);
        assertTrue(CollectionUtil.isEmpty(Config.get().getCoveragePackages()));

        scanAnnotation = Collections.singletonMap("bean1", new SpringUtilTest());
        Mockito.when(context.getBeansWithAnnotation(ComponentScan.class)).thenReturn(scanAnnotation);
        SpringUtil.updateScanBasePackages(context);
        assertTrue(CollectionUtil.isEmpty(Config.get().getCoveragePackages()));
    }

    @Order(4)
    @Test
    void updateScanBasePackagesUpdatesConfigWithBasePackages() {
        ConfigurableApplicationContext context = Mockito.mock(ConfigurableApplicationContext.class);
        Map<String, Object> scanAnnotation = Collections.singletonMap("bean1", new BeanWithComponentScan());
        Mockito.when(context.getBeansWithAnnotation(ComponentScan.class)).thenReturn(scanAnnotation);
        SpringUtil.updateScanBasePackages(context);
        assertEquals("com.example.package", Config.get().getCoveragePackages().iterator().next());
    }

    @Order(5)
    @Test
    void testException() {
        ConfigurableApplicationContext context = Mockito.mock(ConfigurableApplicationContext.class);
        Mockito.when(context.getBeansWithAnnotation(ComponentScan.class)).thenThrow(new RuntimeException());
        Assertions.assertDoesNotThrow(() -> SpringUtil.updateScanBasePackages(context));
    }

    @Order(6)
    @Test
    void testSpringBootApplication() {
        ConfigurableApplicationContext context = Mockito.mock(ConfigurableApplicationContext.class);
        Map<String, Object> scanAnnotation = Collections.singletonMap("bean1", new BeanWithSpringBootApplication());
        Mockito.when(context.getBeansWithAnnotation(ComponentScan.class)).thenReturn(scanAnnotation);
        SpringUtil.updateScanBasePackages(context);
        assertEquals("io.arex.inst.spring", Config.get().getCoveragePackages().iterator().next());
    }

    @Order(7)
    @Test
    void testSpringBootApplicationAndScanBasePackages() {
        ConfigurableApplicationContext context = Mockito.mock(ConfigurableApplicationContext.class);
        Map<String, Object> scanAnnotation = Collections.singletonMap("bean1", new BeanWithSpringBootApplicationAndScanBasePackages());
        Mockito.when(context.getBeansWithAnnotation(ComponentScan.class)).thenReturn(scanAnnotation);
        SpringUtil.updateScanBasePackages(context);
        assertTrue(Config.get().getCoveragePackages().contains("com.example.spring.package"));
        assertEquals(2, Config.get().getCoveragePackages().size());
    }

    @ComponentScan(basePackages = "com.example.package")
    class BeanWithComponentScan {
    }

    @ComponentScan
    class BeanWithoutComponentScan {
    }

    @ComponentScan(basePackages = {})
    class BeanWithNullBasePackages {
    }

    @SpringBootApplication
    class BeanWithSpringBootApplication {
    }

    @SpringBootApplication(scanBasePackages = "com.example.spring.package")
    class BeanWithSpringBootApplicationAndScanBasePackages {
    }

}
