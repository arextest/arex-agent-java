package io.arex.inst.httpservlet.convert;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BodyConvertFactoryTest {


    static BodyConvertFactory bodyConvertFactory = null;
    @BeforeEach
    void setUp() {
        bodyConvertFactory = new BodyConvertFactory();

    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getAll(){
        assertTrue(BodyConvertFactory.getAllList()!=null && BodyConvertFactory.getAllList().size() >0);
    }

}
