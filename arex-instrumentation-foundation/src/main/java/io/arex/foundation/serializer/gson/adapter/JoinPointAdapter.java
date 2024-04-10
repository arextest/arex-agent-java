package io.arex.foundation.serializer.gson.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.arex.foundation.serializer.gson.GsonRequestSerializer;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;

import java.io.IOException;

public class JoinPointAdapter extends TypeAdapter<MethodInvocationProceedingJoinPoint> {

    @Override
    public void write(JsonWriter out, MethodInvocationProceedingJoinPoint value) throws IOException {
        try {
            out.value(value.toString() + GsonRequestSerializer.INSTANCE.serialize(value.getArgs()));
        } catch (Throwable e) {
            out.value(value.toString());
        }
    }

    @Override
    public MethodInvocationProceedingJoinPoint read(JsonReader in) throws IOException {
        throw new UnsupportedOperationException("JointPoint deserialization is not supported.");
    }
}
