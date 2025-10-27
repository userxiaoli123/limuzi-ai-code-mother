package com.limuzi.limuziaicodeapp.service;

public interface ScreenshotService {
    String generateAndUploadScreenshot(String webUrl);

    void deleteFileFromCos(String Key);
}
