package io.arex.inst.jedis.v4;

import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisSocketFactory;
import redis.clients.jedis.exceptions.JedisException;

import java.util.List;

import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.*;

public class JedisFactoryInstrumentation extends TypeInstrumentation {
    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
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
        public static Jedis onEnter(@Advice.FieldValue("jedisSocketFactory") JedisSocketFactory factory,
                                    @Advice.FieldValue("clientConfig") JedisClientConfig clientConfig) {
            return new JedisWrapper(factory, clientConfig);
        }

        // need throw JedisException, not suppress throwable
        @Advice.OnMethodExit
        public static void  onExit(@Advice.Enter Jedis jedis,
                                   @Advice.Return(readOnly = false) PooledObject<Jedis> result) throws Exception {
            if (jedis == null) {
                return;
            }
            try {
                jedis.connect();
                result = new DefaultPooledObject<>(jedis);
            } catch (JedisException jex) {
                try {
                    jedis.quit();
                } catch (RuntimeException var5) {
                }

                try {
                    jedis.close();
                } catch (RuntimeException var4) {
                }
                throw jex;
            }
        }
    }
}
