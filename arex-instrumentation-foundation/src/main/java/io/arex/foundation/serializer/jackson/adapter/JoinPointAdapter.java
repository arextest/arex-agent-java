package io.arex.foundation.serializer.jackson.adapter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.arex.foundation.serializer.jackson.JacksonRequestSerializer;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;

import java.io.IOException;

public class JoinPointAdapter extends JsonSerializer<MethodInvocationProceedingJoinPoint> {
    @Override
    public void serialize(MethodInvocationProceedingJoinPoint value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        try {
            gen.writeString(value.toString() + JacksonRequestSerializer.INSTANCE.serialize(value.getArgs()));
        } catch (Throwable e) {
            gen.writeString(value.toString());
        }
    }
}
