package cn.bugstack.domain.agent.service;

import cn.bugstack.domain.agent.model.entity.ExecuteCommandEntity;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

/**
 * @Author erbao
 * @description: Agent 服务接口
 * @Date 2026/4/20 20:16
 */
public interface IAgentDispatchService {
    // 这个方法是实现根据不同agent类型，执行不同的链路，所以入参跟execute方法保持一致即可
    void agentDispatch(ExecuteCommandEntity requestParameter, ResponseBodyEmitter emitter);

}
