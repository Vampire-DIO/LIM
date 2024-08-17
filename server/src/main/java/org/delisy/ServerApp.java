package org.delisy;

import lombok.extern.slf4j.Slf4j;
import org.delisy.config.AppConfiguration;
import org.delisy.constant.Constants;
import org.delisy.utils.LocalDataCache;
import org.delisy.utils.ZkUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.Resource;
import java.net.InetAddress;

/**
 * Hello world!
 *
 */
@Slf4j
@SpringBootApplication
public class ServerApp implements CommandLineRunner
{

    @Resource
    private AppConfiguration appConfiguration;

    @Resource
    private ZkUtils zkUtils;


    @Value("${server.port}")
    private int httpPort;

    public static void main( String[] args )
    {
        SpringApplication.run(ServerApp.class, args);
    }

    public void run(String... args) throws Exception {
        String ip = InetAddress.getLocalHost().getHostAddress();
        int imPort = appConfiguration.getImPort();
        zkUtils.createRootNode();
        String path = appConfiguration.getZkRoot() + "/" + ip + ":" + imPort  + ":" + httpPort;
        zkUtils.delNode(path);
        zkUtils.createNode(path);
        LocalDataCache.set(Constants.SERVER_URL, path);
        log.info("服务启动成功,服务地址：{}", path);
    }
}
