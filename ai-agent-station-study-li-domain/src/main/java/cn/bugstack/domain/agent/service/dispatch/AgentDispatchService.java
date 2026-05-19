package cn.bugstack.domain.agent.service.dispatch;

import cn.bugstack.domain.agent.adapter.repository.IAgentRepository;
import cn.bugstack.domain.agent.model.entity.ExecuteCommandEntity;
import cn.bugstack.domain.agent.model.valobj.AiAgentVO;
import cn.bugstack.domain.agent.service.IAgentDispatchService;
import cn.bugstack.domain.agent.service.IExecuteStrategy;
import cn.bugstack.types.exception.BizException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author erbao
 * @description: Agent 服务接口
 * @Date 2026/4/20 20:25
 */
@Slf4j
@Service
public class AgentDispatchService implements IAgentDispatchService {
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private Map<String, IExecuteStrategy> executeStrategyMap;
    @Resource
    private IAgentRepository repository;

    @Override
    public void agentDispatch(ExecuteCommandEntity requestParameter, ResponseBodyEmitter emitter) {
        // 根据agent类型执行后续链路
        AiAgentVO aiAgentVO = repository.queryAiAgentById(requestParameter.getAiAgentId());
        String strategy = aiAgentVO.getStrategy();
        IExecuteStrategy executeStrategy = executeStrategyMap.get(strategy);

        if (null == executeStrategy) {
            throw new BizException("不存在的执行策略类型 strategy:" + strategy);
        }

        // 3. 异步执行AutoAgent
        threadPoolExecutor.execute(() -> {
            try {
                executeStrategy.execute(requestParameter,emitter);
            } catch (Exception e) {
                log.error("Agent执行异常：{}", e.getMessage(), e);
                try {
                    emitter.send("执行异常：" + e.getMessage());
                }catch (Exception ex){
                    log.error("发送异常信息失败：{}", ex.getMessage(), ex);
                }
            } finally {
                try {
                    emitter.complete();
                } catch (Exception e) {
                    log.error("完成流式输出失败：{}", e.getMessage(), e);
                }
            }
        });

    }
}
