package com.limuzi.limuziaicodeuser.controller;

import cn.hutool.core.bean.BeanUtil;
import com.limuzi.limuziaicodemother.common.annotation.AuthCheck;
import com.limuzi.limuziaicodemother.common.common.BaseResponse;
import com.limuzi.limuziaicodemother.common.common.DeleteRequest;
import com.limuzi.limuziaicodemother.common.common.ResultUtils;
import com.limuzi.limuziaicodemother.common.constant.UserConstant;
import com.limuzi.limuziaicodemother.common.exception.BusinessException;
import com.limuzi.limuziaicodemother.common.exception.ErrorCode;
import com.limuzi.limuziaicodemother.common.exception.ThrowUtils;
import com.limuzi.limuziaicodemother.model.dto.user.*;
import com.limuzi.limuziaicodemother.model.vo.LoginUserVO;
import com.limuzi.limuziaicodemother.model.vo.UserVO;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import com.limuzi.limuziaicodemother.model.entity.User;
import com.limuzi.limuziaicodeuser.service.UserService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

/**
 * 用户 控制层。
 *
 * @author limuzi
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 获取验证码
     * @param email 邮箱
     * @return 验证信息
     */
    @GetMapping("/get/code/for/register")
    public BaseResponse<Void> getCodeForRegister(@RequestParam(value = "email") String email) {
        return ResultUtils.success(null,userService.getCodeForRegister(email));
    }

    /**
     * 找回密码获取验证码
     * @param email 邮箱
     * @return 验证信息
     */
    @GetMapping("/get/code/for/find/password")
    public BaseResponse<Void> getCodeForFindPassword(@RequestParam(value = "email") String email) {
        return ResultUtils.success(null,userService.getCodeForFindPassword(email));
    }

    @GetMapping("/get/code/for/update/email")
    public BaseResponse<Void> getCodeForUpdateEmail(@RequestParam(value = "email") String email) {
        return ResultUtils.success(null,userService.getCodeForUpdateEmail(email));
    }

    /**
     * 邮箱注册
     * @param userRegisterRequest 用户注册请求
     * @return 注册结果
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        return ResultUtils.success(userService.register(userRegisterRequest));
    }

    /**
     * 密码登录
     * @param userLoginRequest 用户登录请求
     * @param request 请求体
     * @return 登录信息
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        return ResultUtils.success(userService.loginByPassword(userAccount, userPassword, request));
    }

    /**
     * 找回密码
     * @param password 密码
     * @param code 验证码
     * @param email 邮箱
     * @return 找回密码结果
     */
    @PutMapping("/find/password")
    public BaseResponse<Boolean> FindPassword(@RequestParam("password") String password,
                                                 @RequestParam("code") String code,
                                                 @RequestParam("email") String email) {
        return ResultUtils.success(userService.getFindPassword(password, code, email));
    }

    /**
     * 修改邮箱
     * @param email 邮箱
     * @param code 验证码
     * @return 修改邮箱结果
     */
    @PutMapping("/update/email")
    public BaseResponse<Boolean> updateEmail(@RequestParam(value = "email") String email,
                                    @RequestParam(value = "code") String code, HttpServletRequest request) {
        return ResultUtils.success(userService.updateEmail(email, code, request));
    }


    /**
     * 用户注册请求
     *
     * @param userRegisterRequest 用户注册请求
     * @return 注册结果
     */
//    @PostMapping("register")
//    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
//        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
//        String userAccount = userRegisterRequest.getUserAccount();
//        String userPassword = userRegisterRequest.getUserPassword();
//        String checkPassword = userRegisterRequest.getCheckPassword();
//        long id = userService.userRegister(userAccount, userPassword, checkPassword);
//        return ResultUtils.success(id);
//    }



    /**
     * 用户登录请求
     *
     * @param userLoginRequest 请求
     * @return 登录结果
     */
//    @PostMapping("login")
//    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
//        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
//        String userAccount = userLoginRequest.getUserAccount();
//        String userPassword = userLoginRequest.getUserPassword();
//        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
//        return ResultUtils.success(loginUserVO);
//    }

    /**
     * 获取当前登录用户。
     * @param request 请求体
     * @return 登录用户
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }

    /**
     * 退出登录。
     *
     * @param request 请求体
     * @return 退出登录结果
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 创建用户
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtil.copyProperties(userAddRequest, user);
        // 默认密码 12345678
        final String DEFAULT_PASSWORD = "12345678";
        String encryptPassword = userService.getEncryptPassword(DEFAULT_PASSWORD);
        user.setUserPassword(encryptPassword);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 根据 id 获取用户（仅管理员）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) {
        BaseResponse<User> response = getUserById(id);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 删除用户
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if (Objects.equals(loginUser.getUserRole(), UserConstant.ADMIN_ROLE)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "管理员权限不足删除管理员");
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 分页获取用户封装列表（仅管理员）
     *
     * @param userQueryRequest 查询请求参数
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = userQueryRequest.getPageNum();
        long pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(Page.of(pageNum, pageSize),
                userService.getQueryWrapper(userQueryRequest));
        // 数据脱敏
        Page<UserVO> userVOPage = new Page<>(pageNum, pageSize, userPage.getTotalRow());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }

    /**
     * 更新用户头像
     * @param file  文件
     * @param request 请求体
     * @return 头像地址
     */
    @PostMapping(value = "/update/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<String> updateAvatar(@RequestPart("file") MultipartFile file, HttpServletRequest request) {
        String avatarUrl = userService.updateAvatar(file, request);
        return ResultUtils.success(avatarUrl);
    }

}
