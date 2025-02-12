package io.arex.inst.spring;

import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.model.ArexConstants;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SpringUtil {
    public static void updateScanBasePackages(ConfigurableApplicationContext context) {
        try {
            Map<String, Object> scanAnnotation = context.getBeansWithAnnotation(ComponentScan.class);
            Set<String> allBasePackages = new HashSet<>();
            for (Object value : scanAnnotation.values()) {
                Class<?> targetClass = value.getClass();
                String name = targetClass.getName();
                if (name.contains("$$")) {
                    targetClass = targetClass.getSuperclass();
                }
                SpringBootApplication annotation = targetClass.getAnnotation(SpringBootApplication.class);
                if (annotation != null) {
                    allBasePackages.add(targetClass.getPackage().getName());
                    String[] scanBasePackages = annotation.scanBasePackages();
                    allBasePackages.addAll(normalizeBasePackages(scanBasePackages));
                }
                ComponentScan componentScan = targetClass.getAnnotation(ComponentScan.class);
                if (componentScan != null) {
                    String[] basePackages = componentScan.basePackages();
                    allBasePackages.addAll(normalizeBasePackages(basePackages));
                }
            }
            if (allBasePackages.isEmpty()) {
                return;
            }
            System.setProperty(ArexConstants.SPRING_SCAN_PACKAGES, StringUtil.join(allBasePackages, ","));
            Set<String> coveragePackages = Config.get().getCoveragePackages();
            // If there is no config coverage package, the coverage package is set to all base packages
            if (CollectionUtil.isEmpty(coveragePackages)) {
                coveragePackages.addAll(allBasePackages);
            }
        }catch (Exception e) {
            LogManager.warn("spring.scan.packages", e);
        }
    }

    private static Set<String> normalizeBasePackages(String[] basePackages) {
        Set<String> normalizedPackages = new HashSet<>();
        for (String basePackage : basePackages) {
            normalizedPackages.add(StringUtil.replace(basePackage, "*", ""));
        }
        return normalizedPackages;
    }
}
