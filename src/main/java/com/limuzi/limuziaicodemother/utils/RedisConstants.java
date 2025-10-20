package com.limuzi.limuziaicodemother.utils;

public class RedisConstants {
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final String FIND_AND_UPDATE_PASSWORD_CODE_KEY = "findAndUpdate:password:code:";
    public static final String REGISTER_CODE_KEY = "register:code:";
    public static final String UPDATE_EMAIL_CODE_KEY = "update:email:code:";
    public static final String LOGIN_OUT_CODE_KEY = "login:out:code:";
    public static final String LOGIN_TRY_COUNT_KEY = "login:try:count:";
    public static final String LOGIN_USER_KEY = "login:user:";
    public static final String PV_TOTAL_KEY      = "analytics:pv:total";      // 全站总PV
    public static final String PV_DAILY_HASH_KEY = "analytics:pv:daily";      // 按日PV（Hash: field=yyyy-MM-dd, value=long）
}
