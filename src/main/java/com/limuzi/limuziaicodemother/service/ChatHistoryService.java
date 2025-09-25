package com.limuzi.limuziaicodemother.service;

import com.limuzi.limuziaicodemother.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.limuzi.limuziaicodemother.model.entity.User;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.limuzi.limuziaicodemother.model.entity.ChatHistory;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author limuzi
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 获取应用对话历史列表
     * @param appId 应用ID
     * @param pageSize 页面大小
     * @param lastCreateTime 上次创建时间
     * @param loginUser 登录用户
     * @return Page<ChatHistory>
     */
    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                               LocalDateTime lastCreateTime,
                                               User loginUser);

    /**
     *
     * @param appId appID
     * @param message 消息
     * @param messageType 消息类型
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean addChatMessage(Long appId, String message, String messageType, Long userId);

    /**
     * 删除指定appId的对话记录
     * @param appId appId
     * @return 是否成功
     */
    boolean deleteByAppId(Long appId);

    /**
     * 获取查询条件
     * @param chatHistoryQueryRequest 查询条件
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);
}
