package tech.kuiperbelt.lib.common.jpa;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SnowflakeIdWorkerConfiguration {

    /**
     * 数据中心编号, 目前支持 4 个数据中心
     */
    @Value("${tech.kuiperbelt.cluster.id:0}")
    private Long datacenterId;

    /**
     * 机器IP
     */
    @Value("${spring.cloud.client.ip-address:127.0.0.1}")
    private String clientIp;

    /**
     * 子网掩码， 当前算法最大支持同一个子网下有255台机器，超过这个数量，需要将不同的Service 划分在不同的子网中
     */
    @Value("${tech.kuiperbelt.cluster.netmask:255.255.255.0}")
    private String netmask;

    @Bean
    public SnowflakeIdWorker getSnowflakeIdWorker(){
        return SnowflakeIdWorker.getInstance(IpAddressUtil.sequence(clientIp, netmask), datacenterId);
    }


}
