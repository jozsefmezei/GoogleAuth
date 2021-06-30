package hu.ponte.mobile.twoaf.utils;

import com.google.common.io.BaseEncoding;

import java.nio.charset.StandardCharsets;

public class BaseUtils {

    public enum BaseType {BASE_32, BASE_64}

    public byte[] decode32(String value){
        return BaseEncoding.base32().decode(value);
    }

    public byte[] decode64(String value){
        return BaseEncoding.base64().decode(value);
    }

    public String encodeToString32(String value){
        return BaseEncoding.base32().encode(value.getBytes(StandardCharsets.US_ASCII));
    }

    public String encodeToString64(String value){
        return BaseEncoding.base64().encode(value.getBytes(StandardCharsets.US_ASCII));
    }
}
