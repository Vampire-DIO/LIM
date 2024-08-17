package org.delisy.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @Author LvWei
 * @Date 2024/7/29 17:06
 */
@Configuration
@Getter
public class AppConfiguration {

    @Value("${app.zk.root:/root}")
    private String zkRoot;

    @Value("${app.userId:qwe}")
    private String userId;

    @Value("${im.server.port:8082}")
    private int imPort;

}
