package io.arex.foundation.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.arex.foundation.serializer.TimeTestInfo;
import io.arex.foundation.serializer.custom.NumberTypeAdaptor;
import java.util.Arrays;
import javax.xml.datatype.DatatypeConfigurationException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class NumberTypeAdaptorTest {

    static GsonBuilder builder = null;
    @BeforeAll
    static void setUp() {
        builder = new GsonBuilder();
    }

    @AfterAll
    static void tearDown() {
        builder = null;
    }

    @Test
    void test() throws DatatypeConfigurationException {
        final Gson gson = builder.registerTypeAdapterFactory(NumberTypeAdaptor.FACTORY).create();
        final String json = gson.toJson(Arrays.asList(new TimeTestInfo()));
    }
}