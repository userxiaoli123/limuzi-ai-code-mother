package com.limuzi.limuziaicodemother.ai.chat;

import com.limuzi.limuziaicodemother.monitor.MonitorContext;
import com.limuzi.limuziaicodemother.monitor.MonitorContextHolder;
import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.request.ChatRequestParameters;

import java.util.List;
import java.util.Set;

public final class SessionAwareStreamingChatModel implements StreamingChatModel {

    private final StreamingChatModel delegate;
    private final String appId;
    private final String userId;

    public SessionAwareStreamingChatModel(StreamingChatModel delegate, String appId, String userId) {
        this.delegate = delegate;
        this.appId = appId;
        this.userId = userId;
    }

    @Override
    public void chat(dev.langchain4j.model.chat.request.ChatRequest request,
                     dev.langchain4j.model.chat.response.StreamingChatResponseHandler handler) {
        MonitorContextHolder.setContext(MonitorContext.builder()
                .appId(appId)
                .userId(userId != null ? userId : "unknown")
                .build());
        try {
            delegate.chat(request, handler);
        } finally {
            MonitorContextHolder.clearContext();
        }
    }

    // 其余默认方法不必重写（需要的话可委托给 delegate）
    @Override public ChatRequestParameters defaultRequestParameters() { return delegate.defaultRequestParameters(); }
    @Override public List<ChatModelListener> listeners() { return delegate.listeners(); }
    @Override public ModelProvider provider() { return delegate.provider(); }
    @Override public Set<Capability> supportedCapabilities() { return delegate.supportedCapabilities(); }
}
