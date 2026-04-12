package cn.bugstack.domain.agent.service.armory;

import cn.bugstack.domain.agent.model.entity.ArmoryCommandEntity;
import cn.bugstack.domain.agent.model.valobj.AiAgentEnumVO;
import cn.bugstack.domain.agent.model.valobj.AiClientAdvisorTypeEnumVO;
import cn.bugstack.domain.agent.model.valobj.AiClientAdvisorVO;
import cn.bugstack.domain.agent.model.valobj.AiClientApiVO;
import cn.bugstack.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author erbao
 * @description:
 * @Date 2026/4/12 13:53
 */
@Slf4j
@Service
public class AiClientAdvisorNode extends AbstractArmorySupport{
    @Resource
    private AiClientNode aiClientNode;
    @Resource
    private VectorStore vectorStore;
    @Override
    protected String doApply(ArmoryCommandEntity requestParameter, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai Agent 构建，Advisor 构建节点 {}", JSON.toJSONString(requestParameter));

        List<AiClientAdvisorVO> aiClientAdvisorVOList = dynamicContext.getValue(dataName());

        if (aiClientAdvisorVOList == null || aiClientAdvisorVOList.isEmpty()) {
            log.warn("没有需要被初始化的 ai client advisor");
            return router(requestParameter,dynamicContext);
        }

        for (AiClientAdvisorVO aiClientAdvisorVO : aiClientAdvisorVOList) {
            Advisor advisor = createAdvisor(aiClientAdvisorVO);
            registorBean(beanName(aiClientAdvisorVO.getAdvisorId()),Advisor.class,advisor);
        }

        return router(requestParameter,dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return aiClientNode;
    }

    @Override
    protected String beanName(String id) {
        return AiAgentEnumVO.AI_CLIENT_ADVISOR.getBeanName(id);
    }

    @Override
    protected String dataName() {
        return AiAgentEnumVO.AI_CLIENT_ADVISOR.getDataName();
    }

    private Advisor createAdvisor(AiClientAdvisorVO aiClientAdvisorVO) {
        String advisorType = aiClientAdvisorVO.getAdvisorType();
        AiClientAdvisorTypeEnumVO advisorTypeEnum = AiClientAdvisorTypeEnumVO.getAdvisorEnum(advisorType);
        return advisorTypeEnum.createAdvisor(aiClientAdvisorVO, vectorStore);
    }
}
