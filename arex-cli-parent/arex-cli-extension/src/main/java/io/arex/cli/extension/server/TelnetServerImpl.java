package io.arex.cli.extension.server;

import com.google.auto.service.AutoService;
import io.arex.cli.api.extension.CliServer;
import io.arex.cli.extension.server.handler.ServerHandler;
import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.util.NetUtils;
import io.termd.core.telnet.netty.NettyTelnetTtyBootstrap;

import java.util.concurrent.TimeUnit;

/**
 * ServerService
 * @Date: Created in 2022/4/20
 * @Modified By:
 */
@AutoService(CliServer.class)
public class TelnetServerImpl implements CliServer {

    @Override
    public int start() throws Exception {
        String port = ConfigManager.INSTANCE.getServerServiceTcpPort();
        int availablePort = NetUtils.checkTcpPortAvailable(Integer.parseInt(port));
        NettyTelnetTtyBootstrap bootstrap = new NettyTelnetTtyBootstrap()
                .setHost("127.0.0.1").setPort(availablePort);
        bootstrap.start(ServerHandler::handle).get(10, TimeUnit.SECONDS);
        return availablePort;
    }

    @Override
    public String getMode() {
        return "local";
    }
}
