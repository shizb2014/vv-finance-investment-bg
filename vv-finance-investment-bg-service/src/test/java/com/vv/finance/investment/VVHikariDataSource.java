package com.vv.finance.investment;

import cn.hutool.crypto.SecureUtil;
import com.vv.finance.base.utils.StringUtils;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * @Author: szb
 * @CreateTime: 2025-02-24
 * @Description: 1
 * @Version: 1.0
 */
public class VVHikariDataSource  extends HikariDataSource {

    private static final Logger log = LoggerFactory.getLogger(com.vv.finance.base.database.hikari.VVHikariDataSource.class);
    private String passwordDis;
    private static final String PKEY = "0987642874429047";

    public VVHikariDataSource() {
    }

    public String getPassword() {
        if (StringUtils.isNotBlank(this.passwordDis)) {
            return this.passwordDis;
        } else {
            String pkey = System.getProperty("pkey");
            if (StringUtils.trimToNull(pkey) == null) {
                pkey = "0987642874429047";
            }

            String encPassword = super.getPassword();
            if (null == encPassword) {
                return null;
            } else {
                log.info("数据库密码加解密，{" + encPassword + "}, pkey ={}", pkey);
                this.passwordDis = SecureUtil.aes(pkey.getBytes(StandardCharsets.UTF_8)).decryptStr(encPassword);
                return this.passwordDis;
            }
        }
    }

    public void close() {
        super.close();
    }

    public static void main(String[] args) {
        String content = "aAbl*&Ln3332n";
        String pkey = "0987642874429047";
        System.out.println(SecureUtil.aes(pkey.getBytes(StandardCharsets.UTF_8)).encryptHex(content, StandardCharsets.UTF_8));
    }
}
