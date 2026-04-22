package cn.bugstack.domain.agent.service.execute.fixed;

import cn.bugstack.domain.agent.adapter.repository.IAgentRepository;
import cn.bugstack.domain.agent.model.entity.ExecuteCommandEntity;
import cn.bugstack.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
import cn.bugstack.domain.agent.model.valobj.enums.AiAgentEnumVO;
import cn.bugstack.domain.agent.service.IExecuteStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.time.LocalDate;
import java.util.List;

/**
 * @Author erbao
 * @description:
 * @Date 2026/4/22 16:35
 */
@Slf4j
@Service("fixedAgentExecuteStrategy")
public class FixedAgentExecuteStrategy implements IExecuteStrategy {

    @Resource
    protected ApplicationContext applicationContext;

    @Resource
    protected IAgentRepository repository;

    public static final String CHAT_MEMORY_CONVERSATION_ID_KEY = "chat_memory_conversation_id";
    public static final String CHAT_MEMORY_RETRIEVE_SIZE_KEY = "chat_memory_response_size";

    @Override
    public void execute(ExecuteCommandEntity requestParameter, ResponseBodyEmitter emitter) throws Exception {
        // 获取config，遍历config获取client对象以及调用client对象
        List<AiAgentClientFlowConfigVO> aiAgentClientList = repository.queryAiAgentClientFlowConfigByAiAgentId(requestParameter.getAiAgentId());

        // 2. 循环执行客户端
        String content = "";
        for (AiAgentClientFlowConfigVO config : aiAgentClientList) {
            ChatClient chatClient = getChatClientByClientId(config.getClientId());

            content = chatClient.prompt(requestParameter.getMessage() + "，" + content)
                    .system(s -> s.param("current_date", LocalDate.now().toString()))
                    .advisors(a -> a
                            .param(CHAT_MEMORY_CONVERSATION_ID_KEY, requestParameter.getSessionId())
                            .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                    .call().content();

            log.info("智能体对话进行，客户端ID {}", requestParameter.getAiAgentId());
        }

        log.info("智能体对话请求，结果 {} {}", requestParameter.getAiAgentId(), content);
    }

    private ChatClient getChatClientByClientId(String clientId) {
        return getBean(AiAgentEnumVO.AI_CLIENT.getBeanName(clientId));
    }

    private <T> T getBean(String beanName) {
        return (T) applicationContext.getBean(beanName);
    }
}
