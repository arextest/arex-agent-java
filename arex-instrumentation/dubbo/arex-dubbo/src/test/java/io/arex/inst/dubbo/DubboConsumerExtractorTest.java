package io.arex.inst.dubbo;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.util.MockUtils;
import org.apache.dubbo.rpc.RpcInvocation;
import org.apache.dubbo.rpc.support.RpcUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DubboConsumerExtractorTest {
    DubboAdapter adapter = Mockito.mock(DubboAdapter.class);
    DubboConsumerExtractor target = new DubboConsumerExtractor(adapter);

    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(MockUtils.class);
        ArexMocker mocker = new ArexMocker();
        mocker.setTargetRequest(new Mocker.Target());
        mocker.setTargetResponse(new Mocker.Target());
        Mockito.when(MockUtils.createDubboConsumer(any())).thenReturn(mocker);
        Mockito.mockStatic(RpcUtils.class);
    }

    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }

    @Test
    void record() {
        target.record(null);
        verify(adapter).execute(any(), any());
    }

    @Test
    void replay() {
        Mockito.when(MockUtils.replayBody(any())).thenReturn(new NullPointerException());
        Mockito.when(adapter.getInvocation()).thenReturn(Mockito.mock(RpcInvocation.class));
        assertNotNull(target.replay());
        Mockito.when(MockUtils.replayBody(any())).thenReturn("mock");
        assertNotNull(target.replay());
    }
}