package com.limuzi.limuziaicodemother.innerservice;

public interface InnerScreenshotService {

    String generateAndUploadScreenshot(String webUrl);

    void deleteFileFromCos(String coverUrl);

}
