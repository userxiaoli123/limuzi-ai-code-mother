package com.limuzi.limuziaicodemother.core.parser;

import com.limuzi.limuziaicodemother.exception.BusinessException;
import com.limuzi.limuziaicodemother.exception.ErrorCode;
import com.limuzi.limuziaicodemother.model.enums.CodeGenTypeEnum;

/**
 * 代码解析执行器
 *
 * @author 小李
 */
public class CodeParserExecutor {

    private static final HtmlCodeParser htmlCodeParser = new HtmlCodeParser();
    private static final MultiFileCodeParser multiFileCodeParser = new MultiFileCodeParser();

    /**
     * 执行代码解析
     * @param codeContent 代码内容
     * @param codeGenTypeEnum 代码生成类型
     * @return 解析结果
     */
    public static Object executeParser(String codeContent, CodeGenTypeEnum codeGenTypeEnum) {
        return switch (codeGenTypeEnum){
            case HTML -> htmlCodeParser.parseCode(codeContent);
            case MULTI_FILE -> multiFileCodeParser.parseCode(codeContent);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的生成类型");
        };
    }
}
