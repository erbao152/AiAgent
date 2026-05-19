package cn.bugstack.domain.agent.service.armory;

import cn.bugstack.domain.agent.model.entity.ArmoryCommandEntity;
import cn.bugstack.domain.agent.model.valobj.enums.AiAgentEnumVO;
import cn.bugstack.domain.agent.model.valobj.AiClientModelVO;
import cn.bugstack.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson.JSON;
import io.modelcontextprotocol.client.McpSyncClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author erbao
 * @description: 对话模型节点配置
 * @Date 2026/4/11 19:25
 */
@Slf4j
@Service
public class AiClientModelNode extends AbstractArmorySupport {
    @Resource
    private AiClientAdvisorNode aiClientAdvisorNode;
    @Override
    protected String doApply(ArmoryCommandEntity requestParameter, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai Agent 构建节点，AiModel 配置{}", JSON.toJSONString(requestParameter));
        List<AiClientModelVO> aiClientModelVOS = dynamicContext.getValue(dataName());
        if (aiClientModelVOS == null || aiClientModelVOS.isEmpty()) {
            log.warn("没有需要被初始化的 ai client model");
            return router(requestParameter, dynamicContext);
        }

        for (AiClientModelVO aiClientModelVO : aiClientModelVOS) {
            // 获取当前模型关联的 API Bean 对象
            OpenAiApi openAiApi = getBean(AiAgentEnumVO.AI_CLIENT_API.getBeanName(aiClientModelVO.getApiId()));
            if (null == openAiApi) {
                throw new RuntimeException("mode 2 api is null");
            }

            // 获取当前模型关联的 Tool MCP Bean 对象
            List<McpSyncClient> mcpSyncClients = new ArrayList<>();
            for (String toolMcpId : aiClientModelVO.getToolMcpIds()) {
                McpSyncClient mcpSyncClient = getBean(AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getBeanName(toolMcpId));
                mcpSyncClients.add(mcpSyncClient);
            }

            OpenAiChatModel chatModel = OpenAiChatModel.builder()
                    .openAiApi(openAiApi)
                    .defaultOptions(OpenAiChatOptions.builder()
                            .model(aiClientModelVO.getModelName())
                            .toolCallbacks(new SyncMcpToolCallbackProvider(mcpSyncClients).getToolCallbacks())
                            .build())
                    .build();
            registorBean(beanName(aiClientModelVO.getModelId()), OpenAiChatModel.class, chatModel);
        }
        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return aiClientAdvisorNode;
    }

    @Override
    protected String beanName(String id) {
        return AiAgentEnumVO.AI_CLIENT_MODEL.getBeanName(id);
    }

    @Override
    protected String dataName() {
        return AiAgentEnumVO.AI_CLIENT_MODEL.getDataName();
    }
}
