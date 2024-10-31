package io.arex.inst.netty.v4.server;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.netty.v4.common.NettyHelper;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RecordLimiter;
import io.arex.inst.runtime.listener.CaseEventDispatcher;
import io.arex.inst.runtime.util.IgnoreUtils;
import io.arex.inst.runtime.util.MockUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.Attribute;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class ResponseTracingHandlerTest {
    ResponseTracingHandler target;
    ChannelHandlerContext ctx;
    HttpHeaders headers;
    MockedStatic<CaseEventDispatcher> caseEventDispatcherMockStatic;
    ArexMocker mocker;
    HttpResponse httpResponse;
    Channel channel;
    Attribute attribute;
    ChannelPromise promise;
    ArexContext arexContext;

    MockedStatic<ContextManager> contextManagerMockedStatic;
    MockedStatic<MockUtils> mockUtilsStatic;

    @BeforeAll
    void setUpAll() {
        Mockito.mockStatic(IgnoreUtils.class);
        Mockito.mockStatic(NettyHelper.class);
        Mockito.mockStatic(RecordLimiter.class);
        Mockito.mockStatic(Config.class);
        Mockito.when(Config.get()).thenReturn(Mockito.mock(Config.class));
    }

    @AfterAll
    void afterAll() {
        Mockito.clearAllCaches();
    }

    @BeforeEach
    void setUp() {
        target = new ResponseTracingHandler();
        ctx = Mockito.mock(ChannelHandlerContext.class);
        headers = Mockito.mock(HttpHeaders.class);
        caseEventDispatcherMockStatic = Mockito.mockStatic(CaseEventDispatcher.class);
        httpResponse = Mockito.mock(FullHttpResponse.class);
        channel = Mockito.mock(Channel.class);
        attribute = Mockito.mock(Attribute.class);
        promise = Mockito.mock(ChannelPromise.class);
        arexContext = Mockito.mock(ArexContext.class);

        mocker = new ArexMocker();
        mocker.setTargetRequest(new Mocker.Target());
        mocker.setTargetResponse(new Mocker.Target());

        contextManagerMockedStatic = Mockito.mockStatic(ContextManager.class);
        mockUtilsStatic = Mockito.mockStatic(MockUtils.class);
    }

    @AfterEach
    void tearDown() {
        caseEventDispatcherMockStatic.close();
        contextManagerMockedStatic.close();
        mockUtilsStatic.close();
    }

    /**
     * mock:
     * ContextManager.needRecordOrReplay() returns true
     * <p>
     * verify:
     * super.write(ctx, msg, promise) is executed once
     * replayMocker and recordMocker are not executed
     */
    @Test
    void case1() throws Exception {
        Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(false);

        target.write(ctx, httpResponse, promise);
        verify(ctx).write(any(), any());
        mockUtilsStatic.verify(() -> MockUtils.replayMocker(mocker), times(0));
        mockUtilsStatic.verify(() -> MockUtils.recordMocker(mocker), times(0));
    }

    /**
     * mock:
     * ContextManager.needRecordOrReplay() returns true
     * msg is of type HttpResponse
     * <p>
     * verify:
     * super.write(ctx, msg, promise) is executed once
     * replayMocker and recordMocker are not executed
     */
    @Test
    void case2() throws Exception {
        Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
        Mockito.when(ContextManager.needReplay()).thenReturn(true);
        Mockito.when(ContextManager.needRecord()).thenReturn(false);
        Mockito.when(ctx.channel()).thenReturn(channel);
        Mockito.when(channel.attr(any())).thenReturn(attribute);
        Mockito.when(attribute.get()).thenReturn(mocker);
        Mockito.when(NettyHelper.parseBody(any())).thenReturn("mock");
        httpResponse = mock(HttpResponse.class);

        target.write(ctx, httpResponse, promise);
        verify(ctx, times(1)).write(any(), any());
        mockUtilsStatic.verify(() -> MockUtils.replayMocker(mocker), times(0));
        mockUtilsStatic.verify(() -> MockUtils.recordMocker(mocker), times(0));
    }

    /**
     * mock:
     * ContextManager.needRecordOrReplay() returns true
     * needReplay is true
     * needRecord is false
     * <p>
     * verify:
     * super.write(ctx, msg, promise) is executed once
     * MockUtils.replayBody and MockUtils.recordMocker are not executed
     */
    @Test
    void case3() throws Exception {
        Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
        Mockito.when(ContextManager.needReplay()).thenReturn(true);
        Mockito.when(ContextManager.needRecord()).thenReturn(false);
        Mockito.when(ctx.channel()).thenReturn(channel);
        Mockito.when(channel.attr(any())).thenReturn(attribute);
        Mockito.when(attribute.get()).thenReturn(mocker);
        Mockito.when(NettyHelper.parseBody(any())).thenReturn("mock");
        Mockito.when(ContextManager.currentContext()).thenReturn(arexContext);
        Mockito.when(httpResponse.headers()).thenReturn(headers);

        target.write(ctx, httpResponse, promise);
        verify(ctx, times(1)).write(any(), any());
        mockUtilsStatic.verify(() -> MockUtils.replayMocker(mocker), times(1));
        mockUtilsStatic.verify(() -> MockUtils.recordMocker(mocker), times(0));
    }

    /**
     * mock:
     * ContextManager.needRecordOrReplay() returns true
     * needReplay is false
     * needRecord is true
     * <p>
     * verify:
     * super.write(ctx, msg, promise) is executed once
     * MockUtils.replayBody is executed
     */
    @Test
    void case4() throws Exception {
        Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
        Mockito.when(ContextManager.needReplay()).thenReturn(false);
        Mockito.when(ContextManager.needRecord()).thenReturn(true);
        Mockito.when(ctx.channel()).thenReturn(channel);
        Mockito.when(channel.attr(any())).thenReturn(attribute);
        Mockito.when(attribute.get()).thenReturn(mocker);
        Mockito.when(NettyHelper.parseBody(any())).thenReturn("mock");
        Mockito.when(ContextManager.currentContext()).thenReturn(arexContext);
        Mockito.when(httpResponse.headers()).thenReturn(headers);

        target.write(ctx, httpResponse, promise);
        verify(ctx, times(1)).write(any(), any());
        mockUtilsStatic.verify(() -> MockUtils.replayMocker(mocker), times(0));
        mockUtilsStatic.verify(() -> MockUtils.recordMocker(mocker), times(1));
    }
}