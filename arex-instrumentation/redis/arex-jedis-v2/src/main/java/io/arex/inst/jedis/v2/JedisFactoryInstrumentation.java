package io.arex.inst.jedis.v2;

import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.*;

public class JedisFactoryInstrumentation extends TypeInstrumentation {

    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return named("redis.clients.jedis.JedisFactory");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return singletonList(new MethodInstrumentation(
                isMethod().and(named("makeObject")).and(takesArguments(0)),
                this.getClass().getName() + "$MakeObjectAdvice"));
    }

    @SuppressWarnings("unused")
    public static class MakeObjectAdvice {

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static Jedis onEnter(@Advice.FieldValue("hostAndPort") AtomicReference<HostAndPort> hostAndPort,
                                    @Advice.FieldValue("connectionTimeout") Integer connectionTimeout,
                                    @Advice.FieldValue("soTimeout") Integer soTimeout,
                                    @Advice.FieldValue("ssl") Boolean ssl,
                                    @Advice.FieldValue("sslSocketFactory") SSLSocketFactory sslSocketFactory,
                                    @Advice.FieldValue("sslParameters") SSLParameters sslParameters,
                                    @Advice.FieldValue("hostnameVerifier") HostnameVerifier hostnameVerifier) {
            final HostAndPort hp = hostAndPort.get();
            return new JedisWrapper(hp.getHost(), hp.getPort(), connectionTimeout, soTimeout,
                    ssl, sslSocketFactory, sslParameters, hostnameVerifier);
        }

        // todo: change instrumentation: JedisFactory -> DefaultPoolObject
        // need throw JedisException, not suppress throwable
        @Advice.OnMethodExit
        public static void  onExit(@Advice.Enter Jedis jedis,
                                   @Advice.FieldValue("password") String password,
                                   @Advice.FieldValue("database") Integer database,
                                   @Advice.FieldValue("clientName") String clientName,
                                   @Advice.Return(readOnly = false) PooledObject<Jedis> result) throws Exception {
            if (jedis == null) {
                return;
            }
            try {
                jedis.connect();
                if (password != null) {
                    jedis.auth(password);
                }
                if (database != 0) {
                    jedis.select(database);
                }
                if (clientName != null) {
                    jedis.clientSetname(clientName);
                }
                result = new DefaultPooledObject<Jedis>(jedis);
            } catch (JedisException jex) {
                jedis.close();
                throw jex;
            }
        }
    }
}
