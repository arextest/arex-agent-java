package io.arex.inst.httpclient.asynchttpclient.wrapper;

import io.arex.inst.runtime.log.LogManager;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLSession;
import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.HttpResponseStatus;
import org.asynchttpclient.netty.request.NettyRequest;

public class AsyncHandlerWrapper<T> implements AsyncHandler<T> {
    private final AsyncHandler<T> handler;
    private HttpResponseStatus responseStatus;
    private HttpHeaders httpHeaders;
    private final ResponseWrapper responseWrapper;
    private final List<HttpResponseBodyPart> bodyParts = new ArrayList<>(1);

    public AsyncHandlerWrapper(AsyncHandler<T> handler, ResponseWrapper responseWrapper) {
        this.handler = handler;
        this.responseWrapper = responseWrapper;
    }

    @Override
    public State onStatusReceived(HttpResponseStatus responseStatus) throws Exception {
        this.responseStatus = responseStatus;
        return handler.onStatusReceived(responseStatus);
    }

    @Override
    public State onHeadersReceived(HttpHeaders headers) throws Exception {
        this.httpHeaders = this.httpHeaders == null ? headers : this.httpHeaders.add(headers);
        return handler.onHeadersReceived(headers);
    }

    @Override
    public State onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
        if (bodyPart != null && bodyPart.length() > 1) {
            bodyParts.add(bodyPart);
        }
        return handler.onBodyPartReceived(bodyPart);
    }

    @Override
    public void onThrowable(Throwable t) {
        handler.onThrowable(t);
    }

    @Override
    public T onCompleted() throws Exception {
        try {
            responseWrapper.setHttpHeaders(httpHeaders);
            responseWrapper.setHttpStatus(responseStatus);
            responseWrapper.setHttpResponseBody(bodyParts);
        } catch (Throwable ex) {
            LogManager.warn("AsyncHandlerWrapper.onCompleted", ex);
        }
        return handler.onCompleted();
    }

    @Override
    public State onTrailingHeadersReceived(HttpHeaders headers) throws Exception {
        this.httpHeaders = this.httpHeaders == null ? headers : this.httpHeaders.add(headers);
        return handler.onTrailingHeadersReceived(headers);
    }

    @Override
    public void onHostnameResolutionAttempt(String name) {
        handler.onHostnameResolutionAttempt(name);
    }

    @Override
    public void onHostnameResolutionSuccess(String name, List list) {
        handler.onHostnameResolutionSuccess(name, list);
    }

    @Override
    public void onHostnameResolutionFailure(String name, Throwable cause) {
        handler.onHostnameResolutionFailure(name, cause);
    }

    @Override
    public void onTcpConnectAttempt(InetSocketAddress remoteAddress) {
        handler.onTcpConnectAttempt(remoteAddress);
    }

    @Override
    public void onTcpConnectSuccess(InetSocketAddress remoteAddress, Channel connection) {
        handler.onTcpConnectSuccess(remoteAddress, connection);
    }

    @Override
    public void onTcpConnectFailure(InetSocketAddress remoteAddress, Throwable cause) {
        handler.onTcpConnectFailure(remoteAddress, cause);
    }

    @Override
    public void onTlsHandshakeAttempt() {
        handler.onTlsHandshakeAttempt();
    }

    @Override
    public void onTlsHandshakeSuccess(SSLSession sslSession) {
        handler.onTlsHandshakeSuccess(sslSession);
    }

    @Override
    public void onTlsHandshakeFailure(Throwable cause) {
        handler.onTlsHandshakeFailure(cause);
    }

    @Override
    public void onConnectionPoolAttempt() {
        handler.onConnectionPoolAttempt();
    }

    @Override
    public void onConnectionPooled(Channel connection) {
        handler.onConnectionPooled(connection);
    }

    @Override
    public void onConnectionOffer(Channel connection) {
        handler.onConnectionOffer(connection);
    }

    @Override
    public void onRequestSend(NettyRequest request) {
        handler.onRequestSend(request);
    }

    @Override
    public void onRetry() {
        handler.onRetry();
    }
}
