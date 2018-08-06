package hu.ponte.mobile.twoaf.utils;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;

public class BaseUtils {

    public enum BaseType {BASE_32, BASE_64}

    private Base32 codec32 = new Base32();
    private Base64 codec64 = new Base64();

    public byte[] decode32(String value){
        return codec32.decode(value);
    }

    public byte[] decode64(String value){
        return codec64.decode(value);
    }

    public String encodeToString32(String value){
        return codec32.encodeToString(value.getBytes(StandardCharsets.US_ASCII));
    }

    public String encodeToString64(String value){
        return codec64.encodeToString(value.getBytes(StandardCharsets.US_ASCII));
    }

    // Getter --------------------------------------------------------------------------------------
    public Base32 getCodec32() {
        return codec32;
    }

    public Base64 getCodec64() {
        return codec64;
    }
}
