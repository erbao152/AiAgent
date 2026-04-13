package cn.bugstack.domain.agent.model.valobj.enums;

import cn.bugstack.domain.agent.model.valobj.AiClientAdvisorVO;
import cn.bugstack.domain.agent.service.armory.factory.element.RagAnswerAdvisor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author erbao
 * @description:
 * @Date 2026/4/12 13:58
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum AiClientAdvisorTypeEnumVO {
    CHAT_MEMORY("ChatMemory", "上下文记忆（内存模式）") {
        @Override
        public Advisor createAdvisor(AiClientAdvisorVO aiClientAdvisorVO, VectorStore vectorStore) {
            AiClientAdvisorVO.ChatMemory chatMemory = aiClientAdvisorVO.getChatMemory();

            return PromptChatMemoryAdvisor.builder(MessageWindowChatMemory.builder()
                            .maxMessages(chatMemory.getMaxMessages())
                            .build())
                    .build();
        }
    },
    RAG_ANSWER("RagAnswer", "知识库") {
        @Override
        public Advisor createAdvisor(AiClientAdvisorVO aiClientAdvisorVO, VectorStore vectorStore) {
            AiClientAdvisorVO.RagAnswer ragAnswer = aiClientAdvisorVO.getRagAnswer();
            return new RagAnswerAdvisor(vectorStore, SearchRequest.builder()
                    .topK(ragAnswer.getTopK())
                    .filterExpression(ragAnswer.getFilterExpression())
                    .build());
        }
    };

    private String code;
    private String info;

    public abstract Advisor createAdvisor(AiClientAdvisorVO aiClientAdvisorVO, VectorStore vectorStore);

    // 初始化时，将枚举值和对象存入map方便使用
    private static final Map<String, AiClientAdvisorTypeEnumVO> CODE_MAP = new HashMap<>();

    static {
        for (AiClientAdvisorTypeEnumVO advisorTypeEnumVO : values()) {
            CODE_MAP.put(advisorTypeEnumVO.getCode(), advisorTypeEnumVO);
        }
    }

    public static AiClientAdvisorTypeEnumVO getAdvisorEnum(String code) {
        AiClientAdvisorTypeEnumVO advisorTypeEnumVO = CODE_MAP.get(code);
        if (advisorTypeEnumVO == null) {
            throw new RuntimeException("err! advisorType " + code + " not exist!");
        }
        return advisorTypeEnumVO;
    }
}
