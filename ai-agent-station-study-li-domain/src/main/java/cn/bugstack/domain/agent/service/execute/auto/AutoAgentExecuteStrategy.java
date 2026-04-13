package cn.bugstack.domain.agent.service.execute.auto;

import cn.bugstack.domain.agent.model.entity.ExecuteCommandEntity;
import cn.bugstack.domain.agent.service.execute.IExecuteStrategy;
import cn.bugstack.domain.agent.service.execute.auto.step.factory.DefaultAutoAgentExecuteStrategyFactory;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Author erbao
 * @description:
 * @Date 2026/4/13 11:09
 */
@Slf4j
@Service
public class AutoAgentExecuteStrategy implements IExecuteStrategy {
    @Resource
    private DefaultAutoAgentExecuteStrategyFactory factory;

    @Override
    public void execute(ExecuteCommandEntity executeCommandEntity) throws Exception {
        // 调用 auto工厂获取规则树头节点，然后执行规则树
        StrategyHandler<ExecuteCommandEntity, DefaultAutoAgentExecuteStrategyFactory.DynamicContext, String> executeHandler = factory.armoryStrategyHandler();
        String apply = executeHandler.apply(executeCommandEntity, new DefaultAutoAgentExecuteStrategyFactory.DynamicContext());
        log.info("测试结果:{}", apply);
    }
}
