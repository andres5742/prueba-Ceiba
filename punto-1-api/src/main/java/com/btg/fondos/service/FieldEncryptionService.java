package com.btg.fondos.service;

import com.btg.fondos.config.AppProperties;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class FieldEncryptionService {

    private static final String AES_GCM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_BITS = 128;

    private final AppProperties appProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public String encryptIfNeeded(String plainText) {
        if (!StringUtils.hasText(plainText)
                || !StringUtils.hasText(appProperties.getFieldEncryptionKey())) {
            return plainText;
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);
            SecretKey key = deriveKey(appProperties.getFieldEncryptionKey());
            Cipher cipher = Cipher.getInstance(AES_GCM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + cipherText.length);
            buffer.put(iv);
            buffer.put(cipherText);
            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception ex) {
            throw new IllegalStateException("Error cifrando dato", ex);
        }
    }

    public String decryptIfNeeded(String storedValue) {
        if (!StringUtils.hasText(storedValue)
                || !StringUtils.hasText(appProperties.getFieldEncryptionKey())) {
            return storedValue;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(storedValue);
            ByteBuffer buffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[GCM_IV_LENGTH];
            buffer.get(iv);
            byte[] cipherBytes = new byte[buffer.remaining()];
            buffer.get(cipherBytes);
            SecretKey key = deriveKey(appProperties.getFieldEncryptionKey());
            Cipher cipher = Cipher.getInstance(AES_GCM);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] plain = cipher.doFinal(cipherBytes);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return storedValue;
        }
    }

    private static SecretKey deriveKey(String secret) {
        byte[] raw = secret.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = new byte[32];
        for (int i = 0; i < raw.length && i < 32; i++) {
            keyBytes[i] = raw[i];
        }
        return new SecretKeySpec(keyBytes, "AES");
    }
}
