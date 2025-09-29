package com.limuzi.limuziaicodemother.service.impl;

import com.limuzi.limuziaicodemother.service.ScreenshotService;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@Slf4j
@SpringBootTest
class ScreenshotServiceImplTest {
    @Resource
    private ScreenshotService screenshotService;

    @Test
    void generateAndUploadScreenshot() {
        String url = screenshotService.generateAndUploadScreenshot("https://www.baidu.com");
        log.info("截图URL: {}", url);
    }

    @Test
    void deleteFileFromCos() {
        String name = "limuzi-ai-code-mother/screenshots/2025/09/28/a268ab36_compressed.jpg";
        screenshotService.deleteFileFromCos(name);
    }
}