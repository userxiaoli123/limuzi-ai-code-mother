package com.limuzi.limuziaicodemother.controller;

import com.limuzi.limuziaicodemother.langgraph4j.CodeGenWorkflow;
import com.limuzi.limuziaicodemother.langgraph4j.state.WorkflowContext;
import com.limuzi.limuziaicodemother.test.FluxTest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * 工作流 SSE 控制器
 * 演示 LangGraph4j 工作流的流式输出功能
 */
@RestController
@RequestMapping("/flux/test")
@Slf4j
public class FluxTestController {
    /**
     * Flux 流式执行工作流
     */
    @GetMapping(value = "/execute-flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> executeWorkflowWithFlux() {
        log.info("收到 Flux 工作流执行请求");
        return FluxTest.FluxStream();
    }
}
