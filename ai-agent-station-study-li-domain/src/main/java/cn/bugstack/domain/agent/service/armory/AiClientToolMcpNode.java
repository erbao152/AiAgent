package cn.bugstack.domain.agent.service.armory;

import cn.bugstack.domain.agent.adapter.repository.IAgentRepository;
import cn.bugstack.domain.agent.model.entity.ArmoryCommandEntity;
import cn.bugstack.domain.agent.model.valobj.AiAgentEnumVO;
import cn.bugstack.domain.agent.model.valobj.AiClientToolMcpVO;
import cn.bugstack.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson.JSON;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * @Author erbao
 * @description: 将配置中的mcp服务注册为Bean对象
 * @Date 2026/4/11 17:24
 */
@Slf4j
@Service
public class AiClientToolMcpNode extends AbstractArmorySupport{
    @Resource
    private AiClientModelNode aiClientModelNode;

    @Override
    protected String doApply(ArmoryCommandEntity requestParameter, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai Agent 构建节点，Tool MCP 工具配置{}", JSON.toJSONString(requestParameter));

        // 获取mcpVO并注册成为bean对象
        List<AiClientToolMcpVO> aiClientToolMcpVOS = dynamicContext.getValue(AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getDataName());
        if (aiClientToolMcpVOS == null || aiClientToolMcpVOS.isEmpty()){
            log.warn("没有需要被初始化的 ai client tool mcp");
            return router(requestParameter,dynamicContext);
        }

        for (AiClientToolMcpVO aiClientToolMcpVO : aiClientToolMcpVOS) {
            McpSyncClient mcpSyncClient = createMcpSyncClient(aiClientToolMcpVO);

            registorBean(beanName(aiClientToolMcpVO.getMcpId()), McpSyncClient.class,mcpSyncClient);
        }

        return router(requestParameter,dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return aiClientModelNode;
    }

    private McpSyncClient createMcpSyncClient(AiClientToolMcpVO aiClientToolMcpVO){
        String transportType = aiClientToolMcpVO.getTransportType();

        // 判断类型
        switch (transportType){
            case "sse" -> {
                AiClientToolMcpVO.TransportConfigSse transportConfigSse = aiClientToolMcpVO.getTransportConfigSse();
                // http://127.0.0.1:9999/sse?apikey=DElk89iu8Ehhnbu
                // 由baseUri + sseEndpoint 组成
                String originalBaseUri = transportConfigSse.getBaseUri();
                String baseUri;
                String sseEndpoint;

                int queryParamStartIndex = originalBaseUri.indexOf("sse");
                if (queryParamStartIndex != -1){
                    baseUri = originalBaseUri.substring(0,queryParamStartIndex - 1);
                    sseEndpoint = originalBaseUri.substring(queryParamStartIndex - 1);
                }else {
                    baseUri = originalBaseUri;
                    sseEndpoint = transportConfigSse.getSseEndpoint();
                }

                sseEndpoint = StringUtils.isBlank(sseEndpoint) ? "/sse" : sseEndpoint;

                HttpClientSseClientTransport clientSseClientTransport = HttpClientSseClientTransport
                        .builder(baseUri)
                        .sseEndpoint(sseEndpoint)
                        .build();

                McpSyncClient mcpSyncClient = McpClient.sync(clientSseClientTransport).requestTimeout(Duration.ofMinutes(aiClientToolMcpVO.getRequestTimeout())).build();

                var init = mcpSyncClient.initialize();
                System.out.println("SSE MCP Initialized: " + init);

                return mcpSyncClient;
            }
            case "stdio" -> {
                AiClientToolMcpVO.TransportConfigStdio transportConfigStdio = aiClientToolMcpVO.getTransportConfigStdio();
                Map<String, AiClientToolMcpVO.TransportConfigStdio.Stdio> stdioMap = transportConfigStdio.getStdio();
                AiClientToolMcpVO.TransportConfigStdio.Stdio stdio = stdioMap.get(aiClientToolMcpVO.getMcpName());

                var stdioParams = ServerParameters.builder(stdio.getCommand())
                        .args(stdio.getArgs())
                        .env(stdio.getEnv())
                        .build();

                var mcpClient = McpClient.sync(new StdioClientTransport(stdioParams))
                        .requestTimeout(Duration.ofSeconds(aiClientToolMcpVO.getRequestTimeout())).build();
                var init_stdio = mcpClient.initialize();

                log.info("Tool Stdio MCP Initialized {}", init_stdio);
                return mcpClient;
            }
        }

        throw new RuntimeException("err! transportType " + transportType + " not exist!");

    }

    @Override
    protected String beanName(String id) {
        return AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getBeanName(id);
    }

    @Override
    protected String dataName() {
        return AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getDataName();
    }
}
