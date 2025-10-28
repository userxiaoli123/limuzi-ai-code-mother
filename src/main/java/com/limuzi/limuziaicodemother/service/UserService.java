package com.limuzi.limuziaicodemother.service;

import com.limuzi.limuziaicodemother.model.dto.user.UserQueryRequest;
import com.limuzi.limuziaicodemother.model.dto.user.UserRegisterRequest;
import com.limuzi.limuziaicodemother.model.vo.LoginUserVO;
import com.limuzi.limuziaicodemother.model.vo.UserVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.limuzi.limuziaicodemother.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用户 服务层。
 *
 * @author limuzi
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);


    /**
     * 获取加密密码
     *
     * @param userPassword 用户原始密码
     * @return 加密后的密码
     */
    String getEncryptPassword(String userPassword);


    /**
     * 获取脱敏的已登录用户信息
     *
     * @return 脱敏后的用户信息
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @return 脱敏后的用户信息
     */
    UserVO getUserVO(User user);

    /**
     *  获取脱敏的用户信息
     *
     * @return 脱敏后的用户信息
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest 用户查询条件
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);


    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);


    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);


    /**
     * 更新用户头像
     * @param file  文件
     * @return 头像地址
     */
    String updateAvatar(MultipartFile file, HttpServletRequest request);

    /**
     * 获取验证码
     * @param email 邮箱
     * @return 验证码
     */
    String getCodeForRegister(String email);

    /**
     * 用户注册
     * @param userRegisterRequest 用户注册请求
     * @return 注册结果
     */
    Long register(UserRegisterRequest userRegisterRequest);


    /**
     * 用户登录请求
     *
     * @param request 登录请求
     * @return 登录结果
     */
    LoginUserVO loginByPassword(String email, String password, HttpServletRequest request);

    /**
     * 获取验证码
     * @param email 邮箱
     * @return 验证信息
     */
    String getCodeForFindPassword(String email);

    /**
     * 修改邮箱获取验证码
     * @param email 邮箱
     * @return 验证信息
     */
    String getCodeForUpdateEmail(String email);

    /**
     * 找回密码
     * @param password 密码
     * @param code 验证码
     * @param email 邮箱
     * @return 修改结果
     */
    Boolean getFindPassword(String password, String code, String email);

    /**
     * 修改邮箱
     * @param email 邮箱
     * @param code 验证码
     * @return 修改结果
     */
    Boolean updateEmail(String email, String code, HttpServletRequest request);
}
