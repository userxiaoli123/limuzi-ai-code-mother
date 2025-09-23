package com.limuzi.limuziaicodemother.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.limuzi.limuziaicodemother.exception.BusinessException;
import com.limuzi.limuziaicodemother.exception.ErrorCode;
import com.limuzi.limuziaicodemother.model.enums.CodeGenTypeEnum;
import org.apache.commons.lang3.Validate;
import org.aspectj.weaver.IUnwovenClassFile;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * @author 小李
 * @time 2023/9/27 16:01
 */

public abstract class CodeFileSaverTemplate<T> {

    // 文件保存根目录
    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 保存代码
     * @param result 生成结果
     * @return  保存的目录
     */
    public final File saveCode(T result){
        // 1. 验证输入
        validateInput(result);
        // 2. 构建唯一目录
        String baseDirPath = buildUniqueDir();
        // 3. 保存文件
        saveFiles(result, baseDirPath);
        // 4. 返回目录
        return new File(baseDirPath);
    }

    /**
     * 验证输入
     */
    protected void validateInput(T result) {
        if (result == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR, "code is null");
        }
    }


    /**
     * 构建唯一目录路径：tmp/code_output/bizType_雪花ID
     */
    private String buildUniqueDir() {
        String codeGenType = getCodeType().getValue();
        String uniqueDirName = StrUtil.format("{}_{}", codeGenType, IdUtil.getSnowflakeNextIdStr());
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 写入单个文件
     */
    public final void writeToFile(String dirPath, String filename, String content) {
        if (StrUtil.isNotBlank(content)){
            String filePath = dirPath + File.separator + filename;
            FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
        }
    }

    /**
     * 获取代码生成类型
     * @return  代码生成类型枚举
     */
    protected abstract CodeGenTypeEnum getCodeType();

    /**
     * 保存文件
     * @param result 生成结果
     * @param baseDirPath 基础目录路径
     */
    protected abstract void saveFiles(T result, String baseDirPath);


}
