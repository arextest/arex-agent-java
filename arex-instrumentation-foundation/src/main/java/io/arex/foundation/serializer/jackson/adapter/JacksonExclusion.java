package io.arex.foundation.serializer.jackson.adapter;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.config.listener.SerializeSkipInfoListener;

import java.util.List;

import static io.arex.inst.runtime.serializer.StringSerializable.MYBATIS_PLUS_CLASS_LIST;
import static io.arex.inst.runtime.serializer.StringSerializable.TK_MYBATIS_PLUS_CLASS_LIST;

public class JacksonExclusion extends BeanSerializerModifier {

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                     BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {

        String className = beanDesc.getBeanClass().getName();
        // Special treatment MybatisPlus, only serializes the paramNameValuePairs field of QueryWrapper or UpdateWrapper.
        if (MYBATIS_PLUS_CLASS_LIST.contains(className)) {
            beanProperties.removeIf(beanPropertyWriter -> !StringUtil.equals(beanPropertyWriter.getName(), "paramNameValuePairs"));
        }
        if (TK_MYBATIS_PLUS_CLASS_LIST.contains(className)) {
            beanProperties.removeIf(beanPropertyWriter -> StringUtil.equals(beanPropertyWriter.getName(), "table"));
        }
        beanProperties.removeIf(beanPropertyWriter -> SerializeSkipInfoListener.isSkipField(className, beanPropertyWriter.getName()));
        return beanProperties;
    }
}
