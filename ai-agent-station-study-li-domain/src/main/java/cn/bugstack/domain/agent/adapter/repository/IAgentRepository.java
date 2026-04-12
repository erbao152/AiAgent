package cn.bugstack.domain.agent.adapter.repository;

import cn.bugstack.domain.agent.model.valobj.*;

import java.util.List;
import java.util.Map;

/**
 * @Author erbao
 * @description: agent仓储类
 * @Date 2026/4/8 19:48
 */
public interface IAgentRepository {
    List<AiClientApiVO> queryAiClientApiVOListByClientIds(List<String> clientIdList);

    List<AiClientModelVO> queryAiClientModelVOListByClientIds(List<String> clientIdList);

    List<AiClientToolMcpVO> queryAiClientToolMcpVOByClientIds(List<String> clientIdList);

    List<AiClientSystemPromptVO> AiClientSystemPromptVOByClientIds(List<String> clientIdList);
    Map<String,AiClientSystemPromptVO> AiClientSystemPromptMapByClientIds(List<String> clientIdList);

    List<AiClientAdvisorVO> AiClientAdvisorVOByClientIds(List<String> clientIdList);

    List<AiClientVO> AiClientVOByClientIds(List<String> clientIdList);

    List<AiClientApiVO> queryAiClientApiVOListByModelIds(List<String> modelIdList);

    List<AiClientModelVO> AiClientModelVOByModelIds(List<String> modelIdList);

    List<AiClientToolMcpVO> AiClientToolMcpVOByModelIds(List<String> modelIdList);
}
