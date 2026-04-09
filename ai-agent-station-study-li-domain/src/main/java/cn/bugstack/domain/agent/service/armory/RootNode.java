package cn.bugstack.domain.agent.service.armory;

import cn.bugstack.domain.agent.model.entity.ArmoryCommandEntity;
import cn.bugstack.domain.agent.model.valobj.AiAgentEnumVO;
import cn.bugstack.domain.agent.service.armory.business.data.ILoadDataStrategy;
import cn.bugstack.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @Author erbao
 * @description:
 * @Date 2026/4/8 19:49
 */
@Service
public class RootNode extends AbstractArmorySupport{

    private final Map<String, ILoadDataStrategy> loadDataStrategyMap;
    private final AiClientApiNode aiClientApiNode;

    public RootNode(Map<String, ILoadDataStrategy> loadDataStrategyMap, AiClientApiNode aiClientApiNode) {
        this.loadDataStrategyMap = loadDataStrategyMap;
        this.aiClientApiNode = aiClientApiNode;
    }


    // 在wrench框架中，执行顺序：头结点->apply方法（multiThread方法->doApply方法顺序执行）->doApply方法中调用router方法->router方法调用当前节点的get方法获取下一节点->并执行下一节点的apply方法->下一节点apply方法（multiThread方法->doApply方法顺序执行->router方法->该节点get方法->下一节点apply方法）
    // 多线程操作在multiThread方法中，业务处理在doApply方法，判断下一节点在get方法
    @Override
    protected void multiThread(ArmoryCommandEntity requestParameter, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws ExecutionException, InterruptedException, TimeoutException {
        String commandType = requestParameter.getCommandType();

        AiAgentEnumVO agentEnumVO = AiAgentEnumVO.getByCode(commandType);
        String loadDataStrategyKey = agentEnumVO.getLoadDataStrategy();

        ILoadDataStrategy loadDataStrategy = loadDataStrategyMap.get(loadDataStrategyKey);
        loadDataStrategy.loadData(requestParameter,dynamicContext);
    }

    @Override
    protected String doApply(ArmoryCommandEntity requestParameter, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        // 这里的router方法，是调用node中的get方法获取下一节点，并重复进行apply和get
        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return aiClientApiNode;
    }
}
