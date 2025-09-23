package com.limuzi.limuziaicodemother.core;

import com.limuzi.limuziaicodemother.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorFacadeTest {
    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    void generateAndSaveCode() {
        File file = aiCodeGeneratorFacade.generateAndSaveCode("生成一个登录页面,总共不超过20行代码", CodeGenTypeEnum.MULTI_FILE);
        Assertions.assertNotNull(file);
    }

    @Test
    void generateAndSaveStreamCode() {
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream("生成一个博客页面,总共不超过50行代码，生成前给一些网站的描述", CodeGenTypeEnum.MULTI_FILE);
        List<String> result = codeStream.collectList().block();
        Assertions.assertNotNull(result);
        String completeCode = String.join("", result);
        Assertions.assertNotNull(completeCode);
    }
}