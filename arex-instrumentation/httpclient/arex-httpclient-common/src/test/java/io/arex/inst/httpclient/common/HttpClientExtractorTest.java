package io.arex.inst.httpclient.common;

import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.services.DataService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class HttpClientExtractorTest {
    @Mock
    private HttpClientAdapter<?, Object> adapter;
    @InjectMocks
    private HttpClientExtractor<?, Object> httpClientExtractor;

    @BeforeAll
    public static void beforeClass() throws Exception {
        Mockito.mockStatic(SerializeUtils.class);
        mockRecordSave();
    }

    private static void mockRecordSave() throws Exception {
        // mock data service
        Class<DataService> clazz = DataService.class;
        DataService dataService = mock(clazz);
        doNothing().when(dataService).save(any());
        Field field = clazz.getDeclaredField("INSTANCE");
        field.setAccessible(true);
        // Remove final modifier
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, dataService);
    }

    @Test
    public void recordNullTest() {
        reset(DataService.INSTANCE);
        httpClientExtractor.record((HttpResponseWrapper) null);
        verify(DataService.INSTANCE, never()).save(any());
    }

    @Test
    public void recordResponseTest() {
        Object mockResponse = new Object();
        httpClientExtractor.record(mockResponse);
        verify(DataService.INSTANCE).save(any());
    }

    @Test
    public void recordExceptionTest() throws Exception {
        when(adapter.getUri()).thenReturn(new URI("http://localhost"));
        when(adapter.getMethod()).thenReturn("POST");
        httpClientExtractor.record(new IOException("Connection timeout"));
        verify(DataService.INSTANCE, atLeastOnce()).save(any());
    }

    @Test
    public void replayNullTest() {
        Assertions.assertThrows(ArexDataException.class, () -> {
            httpClientExtractor.replay(null);
        });
    }

    @Test
    public void replayExceptionTest() {
        Assertions.assertThrows(ArexDataException.class, () -> {
            HttpResponseWrapper wrapped = new HttpResponseWrapper();
            wrapped.setException(new ExceptionWrapper(null));
            httpClientExtractor.replay(wrapped);
        });
    }
}