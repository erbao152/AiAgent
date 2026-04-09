package cn.bugstack.domain.agent.service.armory.factory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


/**
 * @Author erbao
 * @description: 工厂类：提供责任树上下文类、
 * @Date 2026/4/8 18:06
 */
@Service
public class DefaultArmoryStrategyFactory {

    // 0408-loadDataStrategy-该节中工厂类定义了DynamicContext，是责任树的上下文类

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext{
        private Map<String, Object> dataObjects = new HashMap<>();

        public <T> void setValue(String key, T value){
            dataObjects.put(key, value);
        }

        public <T> T getValue(String key){
            return (T) dataObjects.get(key);
        }

    }
}
