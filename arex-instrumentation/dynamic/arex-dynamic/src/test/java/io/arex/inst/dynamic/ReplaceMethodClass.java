package io.arex.inst.dynamic;

import java.util.Random;
import java.util.UUID;

public class ReplaceMethodClass {
    public static final Random random = new Random();
    public long currentTimeMillis() {
        long timeMillis = System.currentTimeMillis();
        System.out.println("currentTimeMillis: " + timeMillis);
        return timeMillis;
    }

    public String uuid() {
        String uuid = UUID.randomUUID().toString();
        System.out.println("uuid: " + uuid);
        return uuid;
    }


    public int nextInt() {
        int nextInt = random.nextInt(10);
        System.out.println("nextInt: " + nextInt);
        return nextInt;
    }
}
