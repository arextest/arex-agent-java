package io.arex.inst.cache;

import com.arextest.common.annotation.ArexMock;
import org.springframework.cache.annotation.Cacheable;

@ArexMock
public class TestArexMock {
    @Cacheable(cacheNames = "arexcache", key = "#name + #age")
    @ArexMock(key = "#name + #age")
    public String testWithCacheableAnnotation(String name, int age) {
        return "name: " + name + ", age: " + age;
    }

    @ArexMock(key = "#name + #age")
    public String testWithArexMock(String name, int age) {
        return "name: " + name + ", age: " + age;
    }

    @ArexMock
    public void testReturnVoid() {
        System.out.println("testReturnVoid");
    }

    @ArexMock
    public String testWithoutParameter() {
        return "testWithParameter";
    }
}
