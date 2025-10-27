package com.limuzi.limuziaicodeuser.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.limuzi.limuziaicodemother.exception.BusinessException;
import com.limuzi.limuziaicodemother.exception.ErrorCode;
import com.limuzi.limuziaicodemother.exception.ThrowUtils;
import com.limuzi.limuziaicodemother.manager.CosManager;
import com.limuzi.limuziaicodemother.model.dto.user.UserQueryRequest;
import com.limuzi.limuziaicodemother.model.dto.user.UserRegisterRequest;
import com.limuzi.limuziaicodemother.model.enums.UserRoleEnum;
import com.limuzi.limuziaicodemother.model.vo.LoginUserVO;
import com.limuzi.limuziaicodemother.model.vo.UserVO;
import com.limuzi.limuziaicodemother.utils.RedisConstants;
import com.limuzi.limuziaicodeuser.utils.SendCode;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.limuzi.limuziaicodemother.model.entity.User;
import com.limuzi.limuziaicodeuser.mapper.UserMapper;
import com.limuzi.limuziaicodeuser.service.UserService;
import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.limuzi.limuziaicodemother.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户 服务层实现。
 *
 * @author limuzi
 */
@Slf4j
@AllArgsConstructor
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Resource
    private CosManager cosManager;

    private final SendCode sendCode;
    private final String REGISTER_CODE_KEY = RedisConstants.REGISTER_CODE_KEY;
    private final Long REGISTER_CODE_KEY_TTL = 5L;
    private final String FIND_AND_UPDATE_PASSWORD_CODE_KEY = RedisConstants.FIND_AND_UPDATE_PASSWORD_CODE_KEY;
    private final Long FIND_AND_UPDATE_PASSWORD_CODE_KEY_TTL = 5L;
    private final String UPDATE_EMAIL_CODE_KEY = RedisConstants.UPDATE_EMAIL_CODE_KEY;
    private final Long UPDATE_EMAIL_CODE_KEY_TTL = 5L;
    private final StringRedisTemplate stringRedisTemplate;
    private final String LOGIN_TRY_COUNT_KEY = RedisConstants.LOGIN_TRY_COUNT_KEY;
    private final Long LOGIN_TRY_COUNT_KEY_TTL = 10L;
    private final Integer MAX_TRY_COUNT = 5;

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

    /**
     * 获取注册验证码
     * @param email 邮箱
     * @return 验证信息
     */
    @Override
    public String getCodeForRegister(String email) {
        if (email == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱不能为空");
        }
        if (this.getOne(new QueryWrapper().eq("userAccount", email)) != null){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户已存在");
        }
        return sendEmail(email, REGISTER_CODE_KEY, REGISTER_CODE_KEY_TTL);
    }

    /**
     * 获取找回密码验证码
     * @param email 邮箱
     * @return 验证信息
     */
    @Override
    public String getCodeForFindPassword(String email) {
        if (email == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱不能为空");
        }
        if (this.getOne(new QueryWrapper().eq("userAccount", email)) == null){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "邮箱不存在");
        }
        return sendEmail(email, FIND_AND_UPDATE_PASSWORD_CODE_KEY, FIND_AND_UPDATE_PASSWORD_CODE_KEY_TTL);
    }

    /**
     * 获取修改邮箱验证码
     * @param email 邮箱
     * @return 验证信息
     */
    @Override
    public String getCodeForUpdateEmail(String email) {
        if (email == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱不能为空");
        }
        if (this.getOne(new QueryWrapper().eq("userAccount", email)) != null){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "邮箱已注册");
        }
        return sendEmail(email, UPDATE_EMAIL_CODE_KEY, UPDATE_EMAIL_CODE_KEY_TTL);
    }

    /**
     * 注册
     * @param userRegisterRequest 注册信息
     * @return 注册结果
     */
    @Override
    public Long register(UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest.getUserAccount()==null, ErrorCode.PARAMS_ERROR, "邮箱不能为空");
        ThrowUtils.throwIf(userRegisterRequest.getUserName()==null, ErrorCode.PARAMS_ERROR, "用户名不能为空");
        ThrowUtils.throwIf(userRegisterRequest.getUserPassword()==null, ErrorCode.PARAMS_ERROR, "密码不能为空");
        ThrowUtils.throwIf(userRegisterRequest.getCheckPassword()==null, ErrorCode.PARAMS_ERROR, "确认密码不能为空");
        ThrowUtils.throwIf(userRegisterRequest.getVerificationCode()==null, ErrorCode.PARAMS_ERROR, "验证码不能为空");
        ThrowUtils.throwIf(!userRegisterRequest.getUserPassword().equals(userRegisterRequest.getCheckPassword()), ErrorCode.PARAMS_ERROR, "两次密码不一致");

        //        验证密码长度是否合适
        if (userRegisterRequest.getUserPassword().length() < 8 || userRegisterRequest.getCheckPassword().length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不小于8");
        }
//        校验账号长度
        if (userRegisterRequest.getUserAccount().length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度不小于4");
        }

        if (this.getOne(new QueryWrapper().eq("userAccount", userRegisterRequest.getUserAccount())) != null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱已存在");
        }

        //        比对验证码
        verifyCode(userRegisterRequest.getUserAccount(), userRegisterRequest.getVerificationCode(), REGISTER_CODE_KEY);
//        通过比对
        User user = BeanUtil.copyProperties(userRegisterRequest, User.class);
//        对密码进行加密
        String encryptPassword = getEncryptPassword(user.getUserPassword());
        user.setUserPassword(encryptPassword);
        user.setUserRole(UserRoleEnum.USER.getValue());
//        保存注册的用户
        boolean save = this.save(user);
        if (!save){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败");
        }
        stringRedisTemplate.delete(REGISTER_CODE_KEY + user.getUserAccount());
        return user.getId();
    }

    /**
     * 登录
     * @param email 邮箱
     * @param password 密码
     * @param request 请求
     * @return 登录信息
     */
    @Override
    public LoginUserVO loginByPassword(String email, String password, HttpServletRequest request) {
        String count = stringRedisTemplate.opsForValue().get(LOGIN_TRY_COUNT_KEY + email);
        User user = getOne(new QueryWrapper().eq("userAccount", email));
        if (user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        if (count == null){
            count = "0";
        }
        if (Integer.parseInt(count) >= MAX_TRY_COUNT){
            Long expire = stringRedisTemplate.getExpire(LOGIN_TRY_COUNT_KEY + email, TimeUnit.MINUTES);
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST, "您已尝试登录" + MAX_TRY_COUNT + "次，请" + expire + "分钟后重试");
        }
        if (!getEncryptPassword(password).equals(user.getUserPassword())){
            stringRedisTemplate.opsForValue().increment(LOGIN_TRY_COUNT_KEY + email, 1);
            stringRedisTemplate.expire(LOGIN_TRY_COUNT_KEY + email, LOGIN_TRY_COUNT_KEY_TTL, TimeUnit.MINUTES);
            throw new BusinessException(ErrorCode.PASSWORD_ERROR, "密码错误，您还有" + (MAX_TRY_COUNT - Integer.parseInt(count)) + "次机会");
        }
        LoginUserVO loginUserVO = getLoginUserVO(user);
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        //        封装并生成token
        return loginUserVO;
    }


    /**
     * 找回密码
     * @param password 密码
     * @param code 验证码
     * @param email 邮箱
     * @return 修改结果
     */
    @Override
    public Boolean getFindPassword(String password, String code, String email) {
        ThrowUtils.throwIf(password==null, ErrorCode.PARAMS_ERROR, "密码不能为空");
        ThrowUtils.throwIf(code==null, ErrorCode.PARAMS_ERROR, "验证码不能为空");
        ThrowUtils.throwIf(email==null, ErrorCode.PARAMS_ERROR, "邮箱不能为空");
        if (password.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不足8位");
        }
        User user = this.getOne(new QueryWrapper().eq("userAccount", email));
        ThrowUtils.throwIf(user==null, ErrorCode.PARAMS_ERROR, "用户不存在");
        verifyCode(email, code, FIND_AND_UPDATE_PASSWORD_CODE_KEY);
        user.setUserPassword(getEncryptPassword(password));
        boolean b = updateById(user);
        if (!b){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "修改密码失败");
        }
        stringRedisTemplate.delete(FIND_AND_UPDATE_PASSWORD_CODE_KEY + email);
        return true;
    }

    /**
     * 修改邮箱
     * @param email 邮箱
     * @param code 验证码
     * @return 修改结果
     */
    @Override
    public Boolean updateEmail(String email, String code, HttpServletRequest request) {
        ThrowUtils.throwIf(email==null, ErrorCode.PARAMS_ERROR, "邮箱不能为空");
        ThrowUtils.throwIf(code==null, ErrorCode.PARAMS_ERROR, "验证码不能为空");
        User user = this.getLoginUser(request);
        ThrowUtils.throwIf(user==null, ErrorCode.NOT_LOGIN_ERROR);
        verifyCode(email, code, UPDATE_EMAIL_CODE_KEY);
        user.setUserAccount(email);
        boolean b = updateById(user);
        if (!b){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "修改邮箱失败");
        }
        stringRedisTemplate.delete(UPDATE_EMAIL_CODE_KEY + email);
        return true;
    }


    private String sendEmail(String email, String key, Long ttl) {
        String code = stringRedisTemplate.opsForValue().get(key + email);
        Long expire = stringRedisTemplate.getExpire(key + email, TimeUnit.MINUTES);
        if (code!=null) {
            return "请于" + expire + "分钟后重新发送";
        }
        code = sendCode.generateCode();
//        存入redis并设置过期时间
        stringRedisTemplate.opsForValue()
                .set(key + email, code, ttl, TimeUnit.MINUTES);
//        发送验证码
        try {
            sendCode.sendVerificationCode(email, code);
        } catch (MessagingException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "验证码发送失败");
        }
        return "验证码发送成功，请注意查收";
    }


    //    验证验证码
    private void verifyCode(String email, String newCode, String key) {
        String code = stringRedisTemplate.opsForValue().get(key + email);
        if (newCode==null){
            throw new BusinessException(ErrorCode.CODE_ERROR, "验证码不能为空");
        }
        if (code==null){
            throw new BusinessException(ErrorCode.CODE_ERROR, "验证码已过期");
        }
        if (!newCode.equals(code)){
            throw new BusinessException(ErrorCode.CODE_ERROR, "验证码错误");
        }
    }


}
