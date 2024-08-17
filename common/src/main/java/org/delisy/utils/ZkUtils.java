package org.delisy.utils;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import org.delisy.config.AppConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author LvWei
 * @Date 2024/7/29 17:04
 */
@Component
@Slf4j
public class ZkUtils {
    private ZkClient zkClient;

    @Resource
    private AppConfiguration appConfiguration;


    @Value("${app.zk.address}")
    private String zkAddress;

    @PostConstruct
    private void init(){
        zkClient = new ZkClient(zkAddress);
    }

    /**
     * 创建父级节点
     */
    public void createRootNode() {
        boolean exists = zkClient.exists(appConfiguration.getZkRoot());
        if (exists) {
            return;
        }

        //创建 root
        zkClient.createPersistent(appConfiguration.getZkRoot());
    }

    /**
     * 写入指定节点 临时目录
     *
     * @param path
     */
    public void createNode(String path) {
        zkClient.createEphemeral(path);
    }

    public List<String> getAllNode() {
        List<String> children = zkClient.getChildren(appConfiguration.getZkRoot());
        log.info("Query all node =[{}] success.", JSONUtil.toJsonPrettyStr(children));
        return children;
    }

    public void updateNodeLoadScore(String path) {
        Object score = zkClient.readData(path);
        if (score == null) {
            log.warn("Node [{}] not exists.", path);
            return;
        }
        zkClient.writeData(path, (Integer) score + 1);
    }

    public void delNode(String path){
        zkClient.delete(path);
    }
}
