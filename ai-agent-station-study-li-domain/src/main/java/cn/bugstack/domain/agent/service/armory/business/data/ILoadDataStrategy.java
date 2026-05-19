package cn.bugstack.domain.agent.service.armory.business.data;


import cn.bugstack.domain.agent.model.entity.ArmoryCommandEntity;
import cn.bugstack.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;

/**
 * 数据加载策略
 *
 * @author erbao
 */
public interface ILoadDataStrategy {

    void loadData(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext);

}
