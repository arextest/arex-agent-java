package io.arex.inst.dubbo.alibaba;

import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.RpcResult;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DubboFilterExtractorTest {
    static Invocation invocation;

    @BeforeAll
    static void setUp() {
        invocation = Mockito.mock(Invocation.class);
        Mockito.mockStatic(ContextManager.class);
    }

    @AfterAll
    static void tearDown() {
        invocation = null;
        Mockito.clearAllCaches();
    }

    @Test
    void setResultAttachment() {
        DubboFilterExtractor.setResultAttachment(null, null);
        verify(invocation, times(0)).getAttachment(any(), any());
        Mockito.when(ContextManager.needReplay()).thenReturn(true);
        Mockito.when(ContextManager.currentContext()).thenReturn(ArexContext.of("mock"));
        RpcResult result = new RpcResult("mock");
        DubboFilterExtractor.setResultAttachment(invocation, result);
        verify(invocation, times(1)).getAttachment(any(), any());
    }
}