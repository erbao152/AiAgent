package cn.bugstack.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @Author erbao
 * @description:
 * @Date 2026/4/13 18:46
 */
@Data
@ConfigurationProperties(prefix = "spring.ai.agent.auto-config")
public class AiAgentAutoConfigProperties {
    /**
     * 是否启用AI Agent自动装配
     */
    private boolean enabled = false;

    /**
     * 需要自动装配的客户端ID列表
     */
    private List<String> clientIds;
}
