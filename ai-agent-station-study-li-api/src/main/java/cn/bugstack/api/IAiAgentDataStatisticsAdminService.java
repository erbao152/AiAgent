package cn.bugstack.api;


import cn.bugstack.api.dto.DataStatisticsResponseDTO;
import cn.bugstack.api.response.Response;

/**
 * 数据统计
 * @author erbao
 * 2025/10/4 10:33
 */
public interface IAiAgentDataStatisticsAdminService {

    /**
     * 获取系统数据统计
     * @return 统计数据响应
     */
    Response<DataStatisticsResponseDTO> getDataStatistics();
}
