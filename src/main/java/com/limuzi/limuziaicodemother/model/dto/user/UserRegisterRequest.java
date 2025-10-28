package com.limuzi.limuziaicodemother.model.dto.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 用户注册请求体
 */
@Data
public class UserRegisterRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 确认密码
     */
    private String checkPassword;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 验证码
     */
    private String verificationCode;
}
