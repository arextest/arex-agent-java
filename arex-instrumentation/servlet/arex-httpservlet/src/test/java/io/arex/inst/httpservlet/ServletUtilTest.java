package io.arex.inst.httpservlet;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ServletUtilTest {
    @Test
    void appendUri() {
        assertEquals("http://arextest.com?name=mark", ServletUtil.appendUri("http://arextest.com", "name", "mark"));
        assertEquals("http://arextest.com?name=mark#fragment",
            ServletUtil.appendUri("http://arextest.com#fragment", "name", "mark"));

        assertEquals("http://arextest.com?email=arex.test.com@gmail.com&name=mark",
            ServletUtil.appendUri("http://arextest.com?email=arex.test.com@gmail.com", "name", "mark"));

        assertEquals("http://arextest.com?email=arex.test.com@gmail.com&name=mark#fragment",
            ServletUtil.appendUri("http://arextest.com?email=arex.test.com@gmail.com#fragment", "name", "mark"));
    }

    @Test
    public void getFullPath() {
        assertEquals("/servletpath/controll/action", ServletUtil.getRequestPath("http://arextest.com/servletpath/controll/action"));
        assertEquals("/servletpath/controll/action?k1=v1", ServletUtil.getRequestPath("http://arextest.com/servletpath/controll/action?k1=v1"));
    }

    @Test
    public void matchRequestParams() {
        Map<String, List<String>> requestParams = new HashMap<>();
        requestParams.put("name", new ArrayList<>(Arrays.asList("kimi")));
        requestParams.put("age", new ArrayList<>(Arrays.asList("0")));
        assertFalse(ServletUtil.matchAndRemoveRequestParams(requestParams, "name", "lock"));
        assertTrue(ServletUtil.matchAndRemoveRequestParams(requestParams, "age", "0"));
        assertFalse(ServletUtil.matchAndRemoveRequestParams(Collections.emptyMap(), "name", "lock"));
    }

    @Test
    public void getRequestParams() {
        String queryString = "name=kimi&age=0";
        Map<String, List<String>> requestParams = ServletUtil.getRequestParams(queryString);
        assertEquals(2, requestParams.size());
    }
}
