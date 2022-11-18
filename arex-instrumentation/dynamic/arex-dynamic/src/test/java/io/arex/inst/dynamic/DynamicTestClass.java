package io.arex.inst.dynamic;

import java.util.UUID;

public class DynamicTestClass {

    public long testReturnPrimitiveType() {
        long timeMillis = System.currentTimeMillis();
        System.out.println("testReturnPrimitiveType: " + timeMillis);
        return timeMillis;
    }

    public long testReturnPrimitiveTypeWithParameter(int val) {
        long timeMillis = System.currentTimeMillis();
        System.out.println("testReturnPrimitiveTypeWithParameter: " + timeMillis);
        return val;
    }

    public String testReturnNonPrimitiveType() {
        String uuid = UUID.randomUUID().toString();
        System.out.println("testReturnNonPrimitiveType: " + uuid);
        return uuid;
    }

    public String testReturnNonPrimitiveTypeWithParameter(String val) {
        long timeMillis = System.currentTimeMillis();
        System.out.println("testReturnNonPrimitiveTypeWithParameter: " + timeMillis);
        return val;
    }

    public void testReturnVoid() {
        long timeMillis = System.currentTimeMillis();
        System.out.println("testReturnVoid: " + timeMillis);
    }

    public void testReturnVoidWithParameter(String val) {
        long timeMillis = System.currentTimeMillis();
        System.out.println("testReturnVoidWithParameter: " + timeMillis);
    }
}
