package cn.bugstack.domain.agent.service.execute.auto;

import cn.bugstack.domain.agent.model.entity.AutoAgentExecuteResultEntity;
import cn.bugstack.domain.agent.model.entity.ExecuteCommandEntity;
import cn.bugstack.domain.agent.service.IExecuteStrategy;
import cn.bugstack.domain.agent.service.execute.auto.step.factory.DefaultAutoAgentExecuteStrategyFactory;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

/**
 * @Author erbao
 * @description:
 * @Date 2026/4/13 11:09
 */
@Slf4j
@Service("autoAgentExecuteStrategy")
public class AutoAgentExecuteStrategy implements IExecuteStrategy {
    @Resource
    private DefaultAutoAgentExecuteStrategyFactory factory;

    @Override
    public void execute(ExecuteCommandEntity executeCommandEntity, ResponseBodyEmitter emitter) throws Exception {
        // 调用 auto工厂获取规则树头节点，然后执行规则树
        StrategyHandler<ExecuteCommandEntity, DefaultAutoAgentExecuteStrategyFactory.DynamicContext, String> executeHandler = factory.armoryStrategyHandler();
        // 创建动态上下文并初始化必要字段
        DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext = new DefaultAutoAgentExecuteStrategyFactory.DynamicContext();
        dynamicContext.setMaxStep(executeCommandEntity.getMaxStep() != null ? executeCommandEntity.getMaxStep() : 3);
        dynamicContext.setExecutionHistory(new StringBuilder());
        dynamicContext.setCurrentTask(executeCommandEntity.getMessage());
        dynamicContext.setValue("emitter", emitter);

        String apply = executeHandler.apply(executeCommandEntity, dynamicContext);
        log.info("测试结果:{}", apply);

        // 执行完毕，异步发送结果
        try {
            // 创建信息->组装信息->发送信息
            AutoAgentExecuteResultEntity completeResult = AutoAgentExecuteResultEntity.createCompleteResult(executeCommandEntity.getSessionId());
            String result = "data: "+ JSON.toJSONString(completeResult) + "\n\n";
            emitter.send(result);
        }catch (Exception e){
            log.error("发送完成标识失败：{}", e.getMessage(), e);
        }
    }
}
