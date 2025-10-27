package com.limuzi.limuziaicodemother.service;

public interface ScreenshotService {
    String generateAndUploadScreenshot(String webUrl);

    void deleteFileFromCos(String Key);
}
