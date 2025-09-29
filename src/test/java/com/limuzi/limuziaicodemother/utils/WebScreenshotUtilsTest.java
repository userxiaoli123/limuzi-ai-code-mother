package com.limuzi.limuziaicodemother.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class WebScreenshotUtilsTest {

    @Test
    void saveWebPageScreenshot() {
        String testUrl = "https://www.codefather.cn";
        String screenshotPath = WebScreenshotUtils.saveWebPageScreenshot(testUrl);
        log.info("截图保存路径: {}", screenshotPath);
    }
}