package com.reddiax.rdxvideo.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Utility class for cryptographic operations.
 */
@Component
@Slf4j
public class CryptoUtils {
    
    private static final String EC_ALGORITHM = "EC";
    private static final String SIGNATURE_ALGORITHM = "SHA256withECDSA";
    private static final String AES_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    
    private final KeyPair serverKeyPair;
    
    public CryptoUtils() {
        this.serverKeyPair = generateServerKeyPair();
        log.info("Server key pair initialized");
    }
    
    /**
     * Generate the server's EC key pair for signing and key exchange.
     */
    private KeyPair generateServerKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(EC_ALGORITHM);
            keyGen.initialize(new ECGenParameterSpec("secp256r1"));
            return keyGen.generateKeyPair();
        } catch (Exception e) {
            log.error("Failed to generate server key pair", e);
            throw new RuntimeException("Failed to initialize server crypto", e);
        }
    }
    
    /**
     * Get the server's public key in Base64 format.
     */
    public String getServerPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(serverKeyPair.getPublic().getEncoded());
    }
    
    /**
     * Generate a cryptographically secure random nonce.
     */
    public String generateNonce() {
        byte[] nonce = new byte[32];
        new SecureRandom().nextBytes(nonce);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(nonce);
    }
    
    /**
     * Generate a secure device token.
     */
    public String generateDeviceToken() {
        byte[] token = new byte[64];
        new SecureRandom().nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }
    
    /**
     * Verify an ECDSA signature using the client's public key.
     * 
     * @param publicKeyBase64 The client's public key in Base64 format
     * @param data The original data that was signed
     * @param signatureBase64 The signature in Base64 format
     * @return true if the signature is valid
     */
    public boolean verifySignature(String publicKeyBase64, byte[] data, String signatureBase64) {
        try {
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(EC_ALGORITHM);
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(data);
            
            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);
            return signature.verify(signatureBytes);
        } catch (Exception e) {
            log.error("Signature verification failed", e);
            return false;
        }
    }
    
    /**
     * Sign data using the server's private key.
     */
    public String signData(byte[] data) {
        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(serverKeyPair.getPrivate());
            signature.update(data);
            byte[] signatureBytes = signature.sign();
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (Exception e) {
            log.error("Failed to sign data", e);
            throw new RuntimeException("Signing failed", e);
        }
    }
    
    /**
     * Encrypt data using AES-GCM.
     * The IV is prepended to the ciphertext.
     */
    public String encryptAesGcm(String plaintext, byte[] key) {
        try {
            SecretKey secretKey = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            
            // Prepend IV to ciphertext
            byte[] combined = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);
            
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("Encryption failed", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }
    
    /**
     * Generate a random AES key.
     */
    public byte[] generateAesKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            return keyGen.generateKey().getEncoded();
        } catch (Exception e) {
            log.error("Failed to generate AES key", e);
            throw new RuntimeException("Key generation failed", e);
        }
    }
    
    /**
     * Compute SHA-256 hash of data.
     */
    public String sha256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("Hashing failed", e);
            throw new RuntimeException("Hashing failed", e);
        }
    }
    
    /**
     * Compute SHA-256 hash of bytes.
     */
    public String sha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("Hashing failed", e);
            throw new RuntimeException("Hashing failed", e);
        }
    }
}
