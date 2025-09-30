package com.limuzi.limuziaicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.limuzi.limuziaicodemother.ai.AiCodeGenTypeRoutingService;
import com.limuzi.limuziaicodemother.constant.AppConstant;
import com.limuzi.limuziaicodemother.core.AiCodeGeneratorFacade;
import com.limuzi.limuziaicodemother.core.builder.VueProjectBuilder;
import com.limuzi.limuziaicodemother.core.handler.StreamHandlerExecutor;
import com.limuzi.limuziaicodemother.exception.BusinessException;
import com.limuzi.limuziaicodemother.exception.ErrorCode;
import com.limuzi.limuziaicodemother.exception.ThrowUtils;
import com.limuzi.limuziaicodemother.mapper.AppMapper;
import com.limuzi.limuziaicodemother.model.dto.app.AppAddRequest;
import com.limuzi.limuziaicodemother.model.dto.app.AppQueryRequest;
import com.limuzi.limuziaicodemother.model.entity.App;
import com.limuzi.limuziaicodemother.model.entity.User;
import com.limuzi.limuziaicodemother.model.enums.ChatHistoryMessageTypeEnum;
import com.limuzi.limuziaicodemother.model.enums.CodeGenTypeEnum;
import com.limuzi.limuziaicodemother.model.vo.AppVO;
import com.limuzi.limuziaicodemother.model.vo.UserVO;
import com.limuzi.limuziaicodemother.service.AppService;
import com.limuzi.limuziaicodemother.service.ChatHistoryService;
import com.limuzi.limuziaicodemother.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author limuzi
 */
@Slf4j
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService {

    private final UserService userService;
    private final AiCodeGeneratorFacade aiCodeGeneratorFacade;
    private final ChatHistoryService chatHistoryService;
    private final StreamHandlerExecutor streamHandlerExecutor;
    private final VueProjectBuilder vueProjectBuilder;
    private final ScreenshotServiceImpl screenshotService;
    private final AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService;

    public AppServiceImpl(UserService userService, AiCodeGeneratorFacade aiCodeGeneratorFacade, ChatHistoryService chatHistoryService, StreamHandlerExecutor streamHandlerExecutor, VueProjectBuilder vueProjectBuilder, ScreenshotServiceImpl screenshotService, AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService) {
        this.userService = userService;
        this.aiCodeGeneratorFacade = aiCodeGeneratorFacade;
        this.chatHistoryService = chatHistoryService;
        this.streamHandlerExecutor = streamHandlerExecutor;
        this.vueProjectBuilder = vueProjectBuilder;
        this.screenshotService = screenshotService;
        this.aiCodeGenTypeRoutingService = aiCodeGenTypeRoutingService;
    }

    /**
     * 聊天生成代码
     *
     * @param appId 应用 ID
     * @param message 用户消息
     * @param loginUser 登录用户
     * @return 生成的代码
     */
    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限访问该应用，仅本人可以生成代码
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
        }
        // 4. 获取应用的代码生成类型
        String codeGenTypeStr = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenTypeStr);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        }
//        调用ai前先保存会话记录
        chatHistoryService.addChatMessage(appId, message, ChatHistoryMessageTypeEnum.USER.getValue(), loginUser.getId());
        // 5. 调用 AI 生成代码
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
        return streamHandlerExecutor.doExecute(codeStream, chatHistoryService, appId, loginUser, codeGenTypeEnum);
    }


    /**
     * 创建应用
     * @param appAddRequest 创建应用请求
     * @param loginUser 登录用户
     * @return 应用 ID
     */
    @Override
    public Long createApp(AppAddRequest appAddRequest, User loginUser){
        // 参数校验
        String initPrompt = appAddRequest.getInitPrompt();
        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化 prompt 不能为空");
        // 构造入库对象
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        app.setUserId(loginUser.getId());
        // 应用名称暂时为 initPrompt 前 12 位
        app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 12)));
        //        调用服务生成类型
        CodeGenTypeEnum codeGenTypeEnum = aiCodeGenTypeRoutingService.routeCodeGenType(initPrompt);
        // 暂时设置为多文件生成
        app.setCodeGenType(codeGenTypeEnum.getValue());
        // 插入数据库
        boolean result = this.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return app.getId();
    }

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }



    /**
     * 部署应用
     *
     * @param appId 应用 ID
     * @param loginUser 登录用户
     * @return 部署结果
     */
    @Override
    public String deployApp(Long appId, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限部署该应用，仅本人可以部署
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限部署该应用");
        }
        // 4. 检查是否已有 deployKey
        String deployKey = app.getDeployKey();
        // 没有则生成 6 位 deployKey（大小写字母 + 数字）
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }
        // 5. 获取代码生成类型，构建源目录路径
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        // 6. 检查源目录是否存在
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码不存在，请先生成代码");
        }

//        vue项目特殊处理
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (codeGenTypeEnum == CodeGenTypeEnum.VUE_PROJECT){
            //Vue项目需要构建
            boolean buildSuccess = vueProjectBuilder.buildProject(sourceDirPath);
            ThrowUtils.throwIf(!buildSuccess, ErrorCode.SYSTEM_ERROR, "构建Vue项目失败,请重试");
            // 构建完成后，移动目录
            File distDir = new File(sourceDirPath, "dist");
            ThrowUtils.throwIf(!distDir.exists(), ErrorCode.SYSTEM_ERROR, "构建Vue项目失败,请重试");
            sourceDir = distDir;
        }

        // 8. 复制文件到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署失败：" + e.getMessage());
        }
        // 9. 更新应用的 deployKey 和部署时间
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");
        // 10. 返回可访问的 URL
        String appDeployUrl = String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);
        generateAppScreenshotAsync(appId, appDeployUrl);
        return appDeployUrl;
    }

    /**
     * 生成应用截图
     * @param appId 应用 ID
     * @param appDeployUrl 应用部署 URL
     */
    @Override
    public void generateAppScreenshotAsync(Long appId, String appDeployUrl) {
       Thread.ofVirtual()
               .start(()->{
                   String screenshotUrl = screenshotService.generateAndUploadScreenshot(appDeployUrl);
//                   更新数据库封面
                   App app = getById(appId);
                   String coverUrl = app.getCover();
                   app.setId(appId);
                   app.setCover(screenshotUrl);
                   boolean updateResult = this.updateById(app);
                   if (!updateResult) {
                       log.error("更新应用封面失败");
                       throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新应用封面失败");
                   }
                   if (StrUtil.isNotBlank(coverUrl)) {
                       screenshotService.deleteFileFromCos(coverUrl);
                   }
               });
    }

    /**
     * 删除文件
     * @param oldApp 旧应用
     */
    @Override
    public void deleteFile(App oldApp) {
        try {
            //        判断文件是否部署
            if (oldApp.getDeployKey() != null) {
                String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + oldApp.getDeployKey();
                FileUtil.del(deployDirPath);
            }
            String sourceDirName = oldApp.getCodeGenType() + "_" + oldApp.getId();
            String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
            FileUtil.del(sourceDirPath);
        }catch (Exception e){
            log.error("删除文件失败:{}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除文件失败:" + e.getMessage());
        }
    }

    /**
     * 删除应用关联删除会话历史
     * @param id  id
     * @return  boolean
     */
    @Override
    public boolean removeById(Serializable id) {
        if (id == null){
            return false;
        }
        // 删除会话历史
        Long appId = Long.valueOf(id.toString());
        // 删除关联会话历史
        try {
            chatHistoryService.deleteByAppId(appId);
        } catch (Exception e) {
            log.error("删除应用关联会话历史失败:{}", e.getMessage());
        }
        // 删除
        return super.removeById(id);
    }
}
