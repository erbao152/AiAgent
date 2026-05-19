package cn.bugstack.domain.agent.service.task;


import cn.bugstack.domain.agent.adapter.repository.IAgentRepository;
import cn.bugstack.domain.agent.model.valobj.AiAgentTaskScheduleVO;
import cn.bugstack.domain.agent.service.ITaskService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 智能体执行任务
 *
 * @author erbao
 */
@Service
public class AiAgentTaskService implements ITaskService {

    @Resource
    private IAgentRepository repository;

    @Override
    public List<AiAgentTaskScheduleVO> queryAllValidTaskSchedule() {
        return repository.queryAllValidTaskSchedule();
    }

    @Override
    public List<Long> queryAllInvalidTaskScheduleIds() {
        return repository.queryAllInvalidTaskScheduleIds();
    }

}
