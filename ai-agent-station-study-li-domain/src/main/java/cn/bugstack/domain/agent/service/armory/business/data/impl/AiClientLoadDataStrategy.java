package cn.bugstack.domain.agent.service.armory.business.data.impl;

import cn.bugstack.domain.agent.adapter.repository.IAgentRepository;
import cn.bugstack.domain.agent.model.entity.ArmoryCommandEntity;
import cn.bugstack.domain.agent.model.valobj.*;
import cn.bugstack.domain.agent.service.armory.business.data.ILoadDataStrategy;
import cn.bugstack.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author erbao
 * @description: AiClient加载数据
 * @Date 2026/4/9 10:14
 */
@Slf4j
@Service("aiClientLoadDataStrategy")
public class AiClientLoadDataStrategy implements ILoadDataStrategy {

    @Resource
    private IAgentRepository repository;

    @Resource
    protected ThreadPoolExecutor threadPoolExecutor;

    @Override
    public void loadData(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) {
        // 异步加载AiClient数据, client_api,client_model,mcp,advisor,prompt,client
        List<String> clientIdList = armoryCommandEntity.getCommandIdList();

        // api
        CompletableFuture<List<AiClientApiVO>> aiClientApiListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_api) {}", clientIdList);
            return repository.queryAiClientApiVOListByClientIds(clientIdList);
        }, threadPoolExecutor);

        // model
        CompletableFuture<List<AiClientModelVO>> aiClientModelListFuture = CompletableFuture.supplyAsync(()->{
            log.info("查询配置数据(ai_client_model) {}", clientIdList);
            return repository.queryAiClientModelVOListByClientIds(clientIdList);
        },threadPoolExecutor);

        // mcp
        CompletableFuture<List<AiClientToolMcpVO>> aiClientToolMcpListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_tool_mcp) {}", clientIdList);
            return repository.queryAiClientToolMcpVOByClientIds(clientIdList);
        }, threadPoolExecutor);

        // prompt
        CompletableFuture<List<AiClientSystemPromptVO>> aiClientSystemPromptListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_system_prompt) {}", clientIdList);
            return repository.AiClientSystemPromptVOByClientIds(clientIdList);
        }, threadPoolExecutor);

        // advisor
        CompletableFuture<List<AiClientAdvisorVO>> aiClientAdvisorListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_advisor) {}", clientIdList);
            return repository.AiClientAdvisorVOByClientIds(clientIdList);
        }, threadPoolExecutor);

        // client
        CompletableFuture<List<AiClientVO>> aiClientListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client) {}", clientIdList);
            return repository.AiClientVOByClientIds(clientIdList);
        }, threadPoolExecutor);


    }
}
