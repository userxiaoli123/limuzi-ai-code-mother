package com.limuzi.limuziaicodemother.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.limuzi.limuziaicodemother.ai.chat.SessionAwareStreamingChatModel;
import com.limuzi.limuziaicodemother.ai.guardrail.PromptSafetyInputGuardrail;
import com.limuzi.limuziaicodemother.ai.guardrail.RetryOutputGuardrail;
import com.limuzi.limuziaicodemother.ai.tools.*;
import com.limuzi.limuziaicodemother.exception.BusinessException;
import com.limuzi.limuziaicodemother.exception.ErrorCode;
import com.limuzi.limuziaicodemother.model.enums.CodeGenTypeEnum;
import com.limuzi.limuziaicodemother.service.ChatHistoryOriginalService;
import com.limuzi.limuziaicodemother.service.ChatHistoryService;
import com.limuzi.limuziaicodemother.utils.SpringContextUtil;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Slf4j
@Configuration
public class AiCodeGeneratorServiceFactory {

    @Resource(name = "openAiChatModel")
    private ChatModel chatModel;

    @Resource
    RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private ToolManager toolManager;

    @Resource
    private ChatHistoryOriginalService chatHistoryOriginalService;

    /**
     * AI 服务实例缓存
     * 缓存策略：
     * - 最大缓存 1000 个实例
     * - 写入后 30 分钟过期
     * - 访问后 10 分钟过期
     */
    private final Cache<String, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> {
                log.debug("AI 服务实例被移除，缓存键: {}, 原因: {}", key, cause);
            })
            .build();

    /**
     * 根据appid创建 AiCodeGeneratorService
     * @param appId 应用ID
     * @return AiCodeGeneratorService
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(Long appId, Long userId) {
        return getAiCodeGeneratorService(appId, CodeGenTypeEnum.HTML, userId);
    }


    /**
     * 创建新的 AI 服务实例
     * @param appId 应用ID codeGenType 码生成类型
     * @return AiCodeGeneratorService
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(Long appId, CodeGenTypeEnum codeGenTypeEnum, Long userId) {
        String cacheKey = buildCacheKey(appId, codeGenTypeEnum);
        return serviceCache.get(cacheKey, key->createAiCodeGeneratorService(appId, codeGenTypeEnum, userId));
    }

    /**
     * 创建新的 AI 服务实例
     * @param appId
     * @return
     */
    private AiCodeGeneratorService createAiCodeGeneratorService(Long appId, CodeGenTypeEnum codeGenType, Long userId) {
        log.info("为 appId: {} 创建新的 AI 服务实例", appId);
        AiCodeGeneratorService aiCodeGeneratorService;
        // 根据 appId 构建独立的对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(60)   // 一次工具调用也算一次记忆，maxMessages得设置得大一点，不然模型会失忆一直循环调用工具
                .build();
        // 根据代码生成类型选择不同的模型配置
        switch (codeGenType) {
            case VUE_PROJECT -> {
                // 获取chatModelBean
                StreamingChatModel raw = SpringContextUtil.getBean("reasoningStreamingChatModelPrototype", StreamingChatModel.class);

                StreamingChatModel reasoningStreamingChatModel = new SessionAwareStreamingChatModel(raw, String.valueOf(appId), userId.toString());

                // 从数据库加载历史对话到缓存中，由于多了工具调用相关信息，加载的最大数量稍微多一些
                chatHistoryOriginalService.loadOriginalChatHistoryToMemory(appId, chatMemory, 50);
                // Vue 项目生成使用推理模型
                aiCodeGeneratorService = AiServices.builder(AiCodeGeneratorService.class)
                        .streamingChatModel(reasoningStreamingChatModel)
                        .chatMemoryProvider(memoryId -> chatMemory)
                        .tools(toolManager.getAllTools())
                        .hallucinatedToolNameStrategy(toolExecutionRequest -> ToolExecutionResultMessage.from(
                                toolExecutionRequest, "Error: there is no tool called " + toolExecutionRequest.name()
                        ))
//                        .maxSequentialToolsInvocations(20)
                        .inputGuardrails(new PromptSafetyInputGuardrail())
//                        .outputGuardrails(new RetryOutputGuardrail())
                        .build();
            }
            case HTML, MULTI_FILE -> {
                //获取chatModelBean
                StreamingChatModel openAiStreamingChatModel = SpringContextUtil.getBean("streamingChatModelPrototype", StreamingChatModel.class);
                // 从数据库加载历史对话到缓存中
                chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20);
                // HTML 和多文件生成模式使用默认模型
                aiCodeGeneratorService = AiServices.builder(AiCodeGeneratorService.class)
                        .chatModel(chatModel)
                        .streamingChatModel(openAiStreamingChatModel)
                        .chatMemory(chatMemory)
                        .inputGuardrails(new PromptSafetyInputGuardrail())
//                        .outputGuardrails(new RetryOutputGuardrail())
                        .build();
            }
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型: " + codeGenType.getValue());
        };
        return aiCodeGeneratorService;
    }


    /**
     * 创建 AiCodeGeneratorService
     * @return AiCodeGeneratorService
     */
    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return getAiCodeGeneratorService(0L, 1L);
    }

    /**
     * 构造缓存键
     * @param appId 应用ID
     * @param codeGenType 代码生成类型
     * @return 缓存键
     */
    private String buildCacheKey(Long appId, CodeGenTypeEnum codeGenType) {
        return appId + "_" + codeGenType.getValue();
    }
}




