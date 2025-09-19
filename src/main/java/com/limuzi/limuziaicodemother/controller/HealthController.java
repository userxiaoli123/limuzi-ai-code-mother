package com.limuzi.limuziaicodemother.controller;

import com.limuzi.limuziaicodemother.common.BaseResponse;
import com.limuzi.limuziaicodemother.common.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {
    @Operation(summary = "健康检查")
    @GetMapping
    public BaseResponse<String> health() {
        return ResultUtils.success("success");
    }
}
