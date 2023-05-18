package io.arex.inst.httpservlet;

import static org.junit.jupiter.api.Assertions.*;

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
}
