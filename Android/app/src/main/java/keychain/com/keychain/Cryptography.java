package keychain.com.keychain;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;

/**
 * Created by rsen on 1/20/18.
 */

public class Cryptography {
    /*
    public static void main(String [] args) throws Exception {
        // generate public and private keys
        KeyPair keyPair = buildKeyPair();
        PublicKey pubKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        System.out.println("Public: " + base64EncodeKey(pubKey.getEncoded()));
        System.out.println("Private: " + base64EncodeKey(privateKey.getEncoded()));

        // encrypt the message
        byte [] encrypted = encrypt(privateKey, "This is a secret message");
        System.out.println(new String(encrypted));  // <<encrypted message>>

        // decrypt the message
        byte[] secret = decrypt(pubKey, encrypted);
        System.out.println(new String(secret));     // This is a secret message
    }
    */
    //    KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(bytes));
    //PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));


    public static String base64EncodeKey(byte[] key) {
        return Base64.encodeToString(key, Base64.DEFAULT);
    }

    public static byte[] base64DecodeKey(String base64) {
        return Base64.decode(base64, Base64.DEFAULT);
    }

    public static void buildKeyPair() throws NoSuchAlgorithmException {
        final int keySize = 1024;
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
            keyStore.load(null);

            keyPairGenerator.initialize(new KeyGenParameterSpec.Builder("KeyChainKey",
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setUserAuthenticationRequired(true)
                    .build());

            keyPairGenerator.genKeyPair();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] encrypt(PrivateKey privateKey, String message) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry("KeyChainKey", null);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, privateKeyEntry.getCertificate().getPublicKey());

        return cipher.doFinal(message.getBytes());
    }

    public static byte[] decrypt(PublicKey publicKey, byte [] encrypted) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry("KeyChainKey", null);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKeyEntry.getPrivateKey());

        return cipher.doFinal(encrypted);
    }
}
