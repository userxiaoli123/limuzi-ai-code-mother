package com.limuzi.limuziaicodemother.test;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

@Slf4j
public class FluxTest {
    public static Flux<String> FluxStream() {
        // 1. 定义多个同步业务函数（无参有返回值，用Supplier封装）
        List<Supplier<String>> taskFunctions = Arrays.asList(
                () -> {
                    // 模拟函数1的耗时操作（如数据计算）
                    sleep(1000);
                    return "任务1结果：用户信息查询完成";
                },
                () -> {
                    sleep(800);
                    return "任务2结果：订单列表获取完成";
                },
                () -> {
                    sleep(1200);
                    return "任务3结果：统计数据计算完成";
                }
        );

        // 2. 核心：用Flux.create()实现流式输出
        Flux<String> streamFlux = Flux.create(sink ->
            {
                for (Supplier<String> task : taskFunctions) {
                    // 关键：每个函数执行后，立即发射结果（不等待其他函数）
                    String result = task.get();
                    sink.next(result); // 发射当前函数结果，订阅端可即时接收
                }
                sink.complete(); // 所有函数执行完，标记流结束
            }
        );

        log.info("开始执行流式输出");

        return streamFlux;
    }

    // 辅助方法：模拟耗时操作
    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        Flux<String> streamFlux = FluxTest.FluxStream();
        streamFlux.subscribe(
                result -> System.out.println("收到结果：" + result), // 即时打印每个结果
                error -> System.err.println("错误：" + error.getMessage()),
                () -> System.out.println("所有任务执行完毕，流结束")
        );
    }
}
