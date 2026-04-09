package cn.bugstack.domain.agent.model.entity;

import lombok.Data;

import java.util.List;
import java.util.function.Predicate;

/**
 * @Author erbao
 * @description: 装配命令实体
 * @Date 2026/4/8 19:34
 */
@Data
public class ArmoryCommandEntity {


    /**
     * 命令类型
     */
    private String commandType;

    /**
     * 命令索引（clientId、modelId、apiId...）
     */
    private List<String> commandIdList;
}
