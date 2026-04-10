package com.campusrunner;

import cn.hutool.crypto.digest.DigestUtil;

public class PasswordGenerator {
    public static void main(String[] args) {
        String password = "admin123";
        String hashed = DigestUtil.bcrypt(password);
        System.out.println("Password: " + password);
        System.out.println("Hashed: " + hashed);
        
        // Verify
        boolean match = DigestUtil.bcryptCheck(password, hashed);
        System.out.println("Verify: " + match);
    }
}
