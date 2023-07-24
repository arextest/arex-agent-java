package io.arex.inst.dubbo.lexin;

import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.exchange.ResponseCallback;
import com.alibaba.dubbo.remoting.exchange.ResponseFuture;

public class SimpleFuture implements ResponseFuture {

    private final Object value;

    public SimpleFuture(Object value){
        this.value = value;
    }

    public Object get() throws RemotingException {
        return value;
    }

    public Object get(int timeoutInMillis) throws RemotingException {
        return value;
    }

    public void setCallback(ResponseCallback callback) {
        callback.done(value);
    }

    public boolean isDone() {
        return true;
    }

}
