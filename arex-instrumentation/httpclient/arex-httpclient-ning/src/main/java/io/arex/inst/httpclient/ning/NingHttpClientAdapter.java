package io.arex.inst.httpclient.ning;

import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.client.Request;
import com.ning.http.client.Response;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.httpclient.common.HttpClientAdapter;
import io.arex.inst.httpclient.common.HttpResponseWrapper;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.TypeUtil;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NingHttpClientAdapter implements HttpClientAdapter<Request, Object> {
    private final Request request;

    public NingHttpClientAdapter(Request request) {
        this.request = request;
    }

    @Override
    public String getMethod() {
        return request.getMethod();
    }

    @Override
    public byte[] getRequestBytes() {
        if (request.getByteData() != null) {
            return request.getByteData();
        }

        if (request.getStringData() != null) {
            return request.getStringData().getBytes(StandardCharsets.UTF_8);
        }

        if (request.getCompositeByteData() != null) {
            return CollectionUtil.listToByteArray(request.getCompositeByteData());
        }

        return ZERO_BYTE;
    }

    @Override
    public String getRequestContentType() {
        return getRequestHeader("Content-Type");
    }

    @Override
    public String getRequestHeader(String name) {
        return request.getHeaders().getFirstValue(name);
    }

    @Override
    public URI getUri() {
        try {
            return request.getUri().toJavaNetURI();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * users can process data and return custom types by implementing a custom AsyncHandler.
     * So it needs to be compatible with this situation.
     * default handler returns Response type.
     */
    @Override
    public HttpResponseWrapper wrap(Object response) {
        HttpResponseWrapper httpResponseWrapper = new HttpResponseWrapper();
        try {
            if (response instanceof Response) {
                Response tempResponse = (Response) response;
                httpResponseWrapper.setStatusLine(tempResponse.getStatusText());
                httpResponseWrapper.setContent(getBytesFromString(tempResponse.getResponseBody()));
                httpResponseWrapper.setHeaders(buildHeaders(tempResponse.getHeaders()));
                httpResponseWrapper.setStatusCode(tempResponse.getStatusCode());
                return httpResponseWrapper;
            }
            String responseString = Serializer.serialize(response);
            httpResponseWrapper.setContent(getBytesFromString(responseString));
            httpResponseWrapper.setTypeName(TypeUtil.getName(response));
            return httpResponseWrapper;
        } catch (Exception e) {
            LogManager.warn("ning.wrap", e);
            return httpResponseWrapper;
        }
    }

    private byte[] getBytesFromString(String responseBody) {
        return StringUtil.isEmpty(responseBody) ?
                ZERO_BYTE : responseBody.getBytes(StandardCharsets.UTF_8);
    }

    private List<HttpResponseWrapper.StringTuple> buildHeaders(FluentCaseInsensitiveStringsMap headers) {
        List<HttpResponseWrapper.StringTuple> headerList = new ArrayList<>(headers.size());
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            headerList.add(new HttpResponseWrapper.StringTuple(entry.getKey(), StringUtil.join(entry.getValue(), ",")));
        }
        return headerList;
    }

    @Override
    public Object unwrap(HttpResponseWrapper wrapped) {
        if (StringUtil.isNotEmpty(wrapped.getTypeName())) {
            return Serializer.deserialize(new String(wrapped.getContent(), StandardCharsets.UTF_8), wrapped.getTypeName());
        }
        return new ResponseWrapper(wrapped);
    }
}
