package utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

/**
 * Utility class for AES encryption and decryption.
 * This enables symmetric encryption using a shared secret key.
 */

public class EncryptionUtils {

    private static final String ALGORITHM = "AES";

    public static SecretKey generateKey() throws Exception{
        KeyGenerator generator = KeyGenerator.getInstance(ALGORITHM);
        generator.init(128);
        return generator.generateKey();
    }

    public static String encrypt(String plainText, SecretKey key) throws Exception{
        Cipher cipher = Cipher.getInstance(ALGORITHM); // Initialize cipher with AES
        cipher.init(Cipher.ENCRYPT_MODE, key); // Set cipher to encrypt mode with the given key
        byte[] encrypted = cipher.doFinal(plainText.getBytes()); // Encrypt the text
        return Base64.getEncoder().encodeToString(encrypted); // Encode to Base64 for transmission
    }

    public static String decrypt(String cipherText, SecretKey key) throws Exception{
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key); // Set the cipher to decrypt mode
        byte[] decode = Base64.getDecoder().decode(cipherText); // Decode the Base64 input
        return new String(cipher.doFinal(decode)); // Decrypt and return the original string
    }

    public static SecretKey loadKeyFromBytes(byte[] keyBytes){
        return new SecretKeySpec(keyBytes, ALGORITHM); // Create a key spec from bytes
    }
}
