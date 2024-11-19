package io.codef.api.util;

import io.codef.api.error.CodefError;
import io.codef.api.error.CodefException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;

public class RsaUtil {

    private RsaUtil() {
    }

    public static String encryptRSA(String plainText, PublicKey publicKey) {
        try {
            Cipher cipher = initializeCipher(publicKey);

            byte[] bytePlain = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(bytePlain);
        } catch (Exception exception) {
            throw CodefException.of(CodefError.RSA_ENCRYPTION_ERROR, exception);
        }
    }

    public static PublicKey generatePublicKey(String publicKey) {
        final byte[] decodedPublicKey = Base64.getDecoder().decode(publicKey);

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(new X509EncodedKeySpec(decodedPublicKey));
        } catch (Exception exception) {
            throw CodefException.of(CodefError.RSA_ENCRYPTION_ERROR, exception);
        }
    }

    private static Cipher initializeCipher(PublicKey key) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher;
        } catch (Exception exception) {
            throw CodefException.of(CodefError.RSA_ENCRYPTION_ERROR, exception);
        }
    }
}
