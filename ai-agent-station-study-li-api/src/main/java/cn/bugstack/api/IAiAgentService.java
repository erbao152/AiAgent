package cn.bugstack.api;


import jakarta.servlet.http.HttpServletResponse;
import cn.bugstack.api.dto.AutoAgentRequestDTO;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

/**
 * Ai Agent 服务接口
 *
 * @author xiaofuge bugstack.cn @小傅哥
 * 2025/8/7 17:52
 */
public interface IAiAgentService {

    ResponseBodyEmitter autoAgent(AutoAgentRequestDTO request, HttpServletResponse response);

}
