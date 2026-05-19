package cn.bugstack.domain.agent.service;



import cn.bugstack.domain.agent.model.valobj.AiAgentVO;

import java.util.List;

/**
 * 装配接口
 * @author erbao
 */
public interface IArmoryService {

    List<AiAgentVO> acceptArmoryAllAvailableAgents();

    void acceptArmoryAgent(String agentId);

    List<AiAgentVO> queryAvailableAgents();

}
