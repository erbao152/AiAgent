package cn.bugstack.domain.agent.service.armory;

import cn.bugstack.domain.agent.model.entity.ArmoryCommandEntity;
import cn.bugstack.domain.agent.model.valobj.AiAgentEnumVO;
import cn.bugstack.domain.agent.model.valobj.AiClientApiVO;
import cn.bugstack.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author erbao
 * @description:
 * @Date 2026/4/9 19:38
 */
@Slf4j
@Service
public class AiClientApiNode extends AbstractArmorySupport{
    @Resource
    private AiClientToolMcpNode aiClientToolMcpNode;

    @Override
    protected String doApply(ArmoryCommandEntity requestParameter, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        // 将上下文内容装填到OpenAiApi对象中
        log.info("Ai Agent 构建，API 构建节点 {}", JSON.toJSONString(requestParameter));

        // 获取apiVoList
        List<AiClientApiVO> aiClientApiVOList = dynamicContext.getValue(dataName());

        if (aiClientApiVOList == null || aiClientApiVOList.isEmpty()) {
            log.warn("没有需要被初始化的 ai client api");
            return router(requestParameter,dynamicContext);
        }

        // 遍历该list
        for (AiClientApiVO aiClientApiVO : aiClientApiVOList) {
            OpenAiApi openAiApi = OpenAiApi.builder()
                    .baseUrl(aiClientApiVO.getBaseUrl())
                    .apiKey(aiClientApiVO.getApiKey())
                    .completionsPath(aiClientApiVO.getCompletionsPath())
                    .embeddingsPath(aiClientApiVO.getEmbeddingsPath())
                    .build();
            // 将该OpenAiApi注册成bean对象 到 spring容器中
            registorBean(beanName(aiClientApiVO.getApiId()),OpenAiApi.class,openAiApi);
        }

        return router(requestParameter,dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return aiClientToolMcpNode;
    }

    @Override
    protected String beanName(String id) {
        return AiAgentEnumVO.AI_CLIENT_API.getBeanName(id);
    }

    @Override
    protected String dataName() {
        return AiAgentEnumVO.AI_CLIENT_API.getDataName();
    }
}
