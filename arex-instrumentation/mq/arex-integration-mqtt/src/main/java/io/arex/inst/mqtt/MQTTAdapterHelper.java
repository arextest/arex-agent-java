package io.arex.inst.mqtt;

import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.util.MockUtils;

/**
 * MQTTAdapterHelper
 */
public class MQTTAdapterHelper {

    public static Mocker createMocker(String operationName) {
        Mocker mocker = MockUtils.createMqttConsumer(operationName);
        mocker.getTargetRequest().setType(Byte.class.getName());
        return mocker;
    }
}
