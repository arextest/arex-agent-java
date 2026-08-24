package io.arex.inst.httpclient.asynchttpclient.wrapper;

import java.net.SocketAddress;
import org.asynchttpclient.HttpResponseStatus;
import org.asynchttpclient.uri.Uri;

public class ResponseStatusWrapper extends HttpResponseStatus {
    private final int statusCode;
    private final String statusText;
    private final String localAddress;
    private final String remoteAddress;
    private final int protocolMajorVersion;
    private final int protocolMinorVersion;
    private final String protocolName;
    private final String protocolText;

    public ResponseStatusWrapper(Uri uri, ResponseWrapper responseWrapper) {
        super(uri);
        this.statusCode = responseWrapper.getStatusCode();
        this.statusText = responseWrapper.getStatusText();
        this.localAddress = responseWrapper.getLocalAddress();
        this.remoteAddress = responseWrapper.getRemoteAddress();
        this.protocolMajorVersion = responseWrapper.getProtocolMajorVersion();
        this.protocolMinorVersion = responseWrapper.getProtocolMinorVersion();
        this.protocolName = responseWrapper.getProtocolName();
        this.protocolText = responseWrapper.getProtocolText();
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String getStatusText() {
        return statusText;
    }

    @Override
    public String getProtocolName() {
        return protocolName;
    }

    @Override
    public int getProtocolMajorVersion() {
        return protocolMajorVersion;
    }

    @Override
    public int getProtocolMinorVersion() {
        return protocolMinorVersion;
    }

    @Override
    public String getProtocolText() {
        return protocolText;
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return new SocketAddressWrapper(remoteAddress);
    }

    @Override
    public SocketAddress getLocalAddress() {
        return new SocketAddressWrapper(localAddress);
    }
}
