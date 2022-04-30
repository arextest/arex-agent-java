package io.arex.cli.server;

import com.google.auto.service.AutoService;
import io.arex.cli.server.handler.ServerHandler;
import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.services.ServerService;
import io.arex.foundation.util.NetUtils;
import io.termd.core.telnet.netty.NettyTelnetTtyBootstrap;

import java.util.concurrent.TimeUnit;

/**
 * ServerService
 * @Date: Created in 2022/4/20
 * @Modified By:
 */
@AutoService(ServerService.class)
public class TelnetServer extends ServerService {

    public int start() throws Exception {
        String port = ConfigManager.INSTANCE.getServerServiceTcpPort();
        int availablePort = NetUtils.checkTcpPortAvailable(Integer.parseInt(port));
        NettyTelnetTtyBootstrap bootstrap = new NettyTelnetTtyBootstrap()
                .setHost("127.0.0.1").setPort(availablePort);
        bootstrap.start(ServerHandler::handle).get(10, TimeUnit.SECONDS);
        return availablePort;
    }
}
