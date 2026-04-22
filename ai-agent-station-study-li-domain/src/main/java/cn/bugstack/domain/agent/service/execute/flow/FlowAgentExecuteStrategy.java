package cn.bugstack.domain.agent.service.execute.flow;

import cn.bugstack.domain.agent.model.entity.AutoAgentExecuteResultEntity;
import cn.bugstack.domain.agent.model.entity.ExecuteCommandEntity;
import cn.bugstack.domain.agent.service.IExecuteStrategy;
import cn.bugstack.domain.agent.service.execute.flow.step.factory.DefaultFlowAgentExecuteStrategyFactory;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

/**
 * @Author erbao
 * @description:
 * @Date 2026/4/20 16:12
 */
@Slf4j
@Service("flowAgentExecuteStrategy")
public class FlowAgentExecuteStrategy implements IExecuteStrategy {
    @Resource
    private DefaultFlowAgentExecuteStrategyFactory factory;
    @Override
    public void execute(ExecuteCommandEntity executeCommandEntity, ResponseBodyEmitter emitter) throws Exception {
        StrategyHandler<ExecuteCommandEntity, DefaultFlowAgentExecuteStrategyFactory.DynamicContext, String> executeHandler = factory.armoryStrategyHandler();

        // 拼接上下文
        // 创建动态上下文并初始化必要字段
        DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext = new DefaultFlowAgentExecuteStrategyFactory.DynamicContext();
        dynamicContext.setMaxStep(executeCommandEntity.getMaxStep() != null ? executeCommandEntity.getMaxStep() : 4);
        dynamicContext.setExecutionHistory(new StringBuilder());
        dynamicContext.setCurrentTask(executeCommandEntity.getMessage());
        dynamicContext.setValue("emitter", emitter);

        String apply = executeHandler.apply(executeCommandEntity, dynamicContext);
        log.info("流程执行结果:{}", apply);

        // 发送完成标识
        try {
            AutoAgentExecuteResultEntity completeResult = AutoAgentExecuteResultEntity.createCompleteResult(executeCommandEntity.getSessionId());
            // 发送SSE格式的数据
            String sseData = "data: " + JSON.toJSONString(completeResult) + "\n\n";
            emitter.send(sseData);
        } catch (Exception e) {
            log.error("发送完成标识失败：{}", e.getMessage(), e);
        }
    }
}
