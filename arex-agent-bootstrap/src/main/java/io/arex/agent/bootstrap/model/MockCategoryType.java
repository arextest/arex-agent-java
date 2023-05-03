package io.arex.agent.bootstrap.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MockCategoryType implements Serializable {

    private static final Map<String, MockCategoryType> CATEGORY_TYPE_MAP = new HashMap<>();
    public static final MockCategoryType SERVLET = createEntryPoint("Servlet");
    public static final MockCategoryType DATABASE = createDependency("Database");
    public static final MockCategoryType HTTP_CLIENT = createDependency("HttpClient");
    public static final MockCategoryType CONFIG_FILE = createSkipComparison("ConfigFile");
    public static final MockCategoryType DYNAMIC_CLASS = createSkipComparison("DynamicClass");
    public static final MockCategoryType REDIS = createSkipComparison("Redis");
    public static final MockCategoryType MESSAGE_PRODUCER = createDependency("QMessageProducer");
    public static final MockCategoryType MESSAGE_CONSUMER = createEntryPoint("QMessageConsumer");
    public static final MockCategoryType DUBBO_CONSUMER = createDependency("DubboConsumer");
    public static final MockCategoryType DUBBO_PROVIDER = createEntryPoint("DubboProvider");
    public static final MockCategoryType DUBBO_STREAM_PROVIDER = createDependency("DubboStreamProvider");
    public static final MockCategoryType MQTT_MESSAGE_CONSUMER = createEntryPoint("MqttMessageConsumer");


    private String name;
    private boolean entryPoint;
    private boolean skipComparison;

    public static MockCategoryType createEntryPoint(String name) {
        return create(name, true, false);
    }

    public static MockCategoryType createSkipComparison(String name) {
        return create(name, false, true);
    }

    public static MockCategoryType createDependency(String name) {
        return create(name, false, false);
    }

    public static MockCategoryType create(String name, boolean entryPoint, boolean skipComparison) {
        return CATEGORY_TYPE_MAP.computeIfAbsent(name,
            key -> new MockCategoryType(name, entryPoint, skipComparison));
    }

    public static Collection<MockCategoryType> values() {
        return CATEGORY_TYPE_MAP.values();
    }

    public String getName() {
        return this.name;
    }

    public boolean isEntryPoint() {
        return this.entryPoint;
    }

    public boolean isSkipComparison() {
        return this.skipComparison;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEntryPoint(boolean entryPoint) {
        this.entryPoint = entryPoint;
    }

    public void setSkipComparison(boolean skipComparison) {
        this.skipComparison = skipComparison;
    }

    public MockCategoryType() {
    }

    private MockCategoryType(String name, boolean entryPoint, boolean skipComparison) {
        this.name = name;
        this.entryPoint = entryPoint;
        this.skipComparison = skipComparison;
    }


    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder("MockCategoryType{");
        builder.append("name='").append(name).append('\'');
        builder.append(", entryPoint=").append(entryPoint);
        builder.append(", skipComparison=").append(skipComparison);
        builder.append('}');
        return builder.toString();
    }
}
