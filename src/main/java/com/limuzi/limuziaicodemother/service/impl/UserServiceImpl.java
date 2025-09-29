package com.limuzi.limuziaicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.limuzi.limuziaicodemother.exception.BusinessException;
import com.limuzi.limuziaicodemother.exception.ErrorCode;
import com.limuzi.limuziaicodemother.exception.ThrowUtils;
import com.limuzi.limuziaicodemother.manager.CosManager;
import com.limuzi.limuziaicodemother.model.dto.user.UserQueryRequest;
import com.limuzi.limuziaicodemother.model.enums.UserRoleEnum;
import com.limuzi.limuziaicodemother.model.vo.LoginUserVO;
import com.limuzi.limuziaicodemother.model.vo.UserVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.limuzi.limuziaicodemother.model.entity.User;
import com.limuzi.limuziaicodemother.mapper.UserMapper;
import com.limuzi.limuziaicodemother.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static com.limuzi.limuziaicodemother.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户 服务层实现。
 *
 * @author limuzi
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Resource
    private CosManager cosManager;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
//        校验参数是否为空
        if (userAccount == null || userPassword == null || checkPassword == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
//        验证密码长度是否合适
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不小于8");
        }
//        校验账号长度
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度不小于4");
        }
//        验证两次输入的密码是否一致
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

//       查找是否注册过
        QueryWrapper queryWrapper = new QueryWrapper().eq(User::getUserAccount, userAccount);
        long count = this.mapper.selectCountByQuery(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已注册");
        }

        String encryptPassword = getEncryptPassword(userPassword);

        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("无名");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean save = this.save(user);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败");
        }
        return user.getId();
    }

    @Override
    public String getEncryptPassword(String userPassword) {
//        盐值加密，直接使用hutool工具类
        String salt = "limuzi";
        return DigestUtils.md5DigestAsHex((userPassword + salt).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        return BeanUtil.copyProperties(user, LoginUserVO.class);
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        return BeanUtil.copyProperties(user, UserVO.class);
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (userList == null) {
            return null;
        }
        return userList.stream().map(this::getUserVO).toList();
    }

    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .eq("userRole", userRole)
                .like("userAccount", userAccount)
                .like("userName", userName)
                .like("userProfile", userProfile)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }


    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        if (userAccount == null || userPassword == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        String encryptPassword = getEncryptPassword(userPassword);
        QueryWrapper queryWrapper = new QueryWrapper().eq(User::getUserAccount, userAccount)
                .eq(User::getUserPassword, encryptPassword);
        User user = this.mapper.selectOneByQuery(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        LoginUserVO loginUserVO = getLoginUserVO(user);
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return loginUserVO;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        Object attribute = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) attribute;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        User user = this.mapper.selectOneById(currentUser.getId());
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR);
        return user;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        Object attribute = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (attribute == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public String updateAvatar(MultipartFile file, HttpServletRequest request) {
        String key = cosManager.generateKey(file.getOriginalFilename() + UUID.randomUUID());
        User user = getLoginUser(request);
        Long userId = user.getId();
        User currentUser = getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
//      存储之前的头像路径
        String oldAvatar = currentUser.getUserAvatar();
//        修改头像
        try {
            String newAvatar;
            try (InputStream in = file.getInputStream()) {
                newAvatar = cosManager.uploadFile(key, in, file.getSize(), file.getContentType());
            }
            if (newAvatar != null){
                currentUser.setUserAvatar(newAvatar);
                boolean b = updateById(currentUser);
                if(!b){
//                    删除上传的头像
                    cosManager.deleteFile(newAvatar);
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新用户头像失败");
                }
                if (oldAvatar != null) {
                    cosManager.deleteFile(oldAvatar);
                }
            }
            return newAvatar;
        } catch (IOException e) {
            log.info(e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传头像失败");
        }
    }

}
