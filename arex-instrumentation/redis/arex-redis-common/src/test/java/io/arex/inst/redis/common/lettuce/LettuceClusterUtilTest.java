package io.arex.inst.redis.common.lettuce;

import io.arex.inst.runtime.config.Config;
import io.lettuce.core.protocol.AsyncCommand;
import io.lettuce.core.protocol.Command;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class LettuceClusterUtilTest {
    static Command cmd;
    @BeforeAll
    static void setUp() {
        cmd = Mockito.mock(Command.class);
        Mockito.mockStatic(Config.class);
    }

    @Test
    void testClusterAsynReplay(){
        Config config = Mockito.mock(Config.class);
        Mockito.when(config.getRecordVersion()).thenReturn("0.4.0");
        Mockito.when(Config.get()).thenReturn(config);
        LettuceClusterUtil.clusterAsynReplay("test","key", cmd,"redis://127.0.0.1");

        AsyncCommand asyncCommand = new AsyncCommand(cmd);
        LettuceClusterUtil.clusterAsyncRecord("test",asyncCommand,"test","redis://127.0.0.1");
    }


}
