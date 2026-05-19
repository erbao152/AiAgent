package cn.bugstack.domain.agent.service;


import cn.bugstack.domain.agent.model.entity.ExecuteCommandEntity;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

/**
 * 执行策略接口
 *
 * @author erbao
 */
public interface IExecuteStrategy {

    void execute(ExecuteCommandEntity requestParameter, ResponseBodyEmitter emitter) throws Exception;

}
