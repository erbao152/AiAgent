package cn.bugstack.domain.agent.service;


import cn.bugstack.domain.agent.model.valobj.AiAgentTaskScheduleVO;

import java.util.List;

/**
 * 智能体执行任务
 * @author erbao
 */
public interface ITaskService {

    List<AiAgentTaskScheduleVO> queryAllValidTaskSchedule();

    List<Long> queryAllInvalidTaskScheduleIds();

}
