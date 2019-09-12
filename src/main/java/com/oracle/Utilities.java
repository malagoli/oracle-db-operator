package com.oracle;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Utilities {
    private static final Logger log = LoggerFactory.getLogger(Utilities.class.getName());

    public static String encodeBase64(String inp) {
        byte[] encodedBytes = Base64.getEncoder().encode(inp.getBytes());
        return new String(encodedBytes);
    }

    public static String decodeBase64(String s) {
        byte[] b =  Base64.getDecoder().decode(s);
        return new String(b, StandardCharsets.UTF_8);
    }

    public static String randomPassword() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        return RandomStringUtils.random( 15, characters );
    }

    public static String getEnv(String env) {
        String ret = System.getenv(env);

        if(ret == null) {
            errorAndExit("Unable to find environment variable ["+ env +"]");
        }

        return ret;
    }

    public static boolean isEnvSet(String env) {
        String ret = System.getenv(env);

        if(ret == null) {
            return false;
        }

        return true;
    }


    public static void errorAndExit(String error) {
        log.error(error);
        System.exit(1);
    }

    public static String sanitizePDBName(String pdbName) {
        return pdbName.replaceAll("-", "_");
    }
}
