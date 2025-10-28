package com.limuzi.limuziaicodemother.service.impl;

import com.limuzi.limuziaicodemother.innerservice.InnerScreenshotService;
import com.limuzi.limuziaicodemother.service.ScreenshotService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
@Slf4j
public class InnerScreenshotServiceImpl implements InnerScreenshotService {

    @Resource
    private ScreenshotService screenshotService;

    @Override
    public String generateAndUploadScreenshot(String webUrl) {
        return screenshotService.generateAndUploadScreenshot(webUrl);
    }

    @Override
    public void deleteFileFromCos(String coverUrl) {
        screenshotService.deleteFileFromCos(coverUrl);
    }
}
