package keychain.com.keychain;

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

    public static KeyPair buildKeyPair() throws NoSuchAlgorithmException {
        final int keySize = 1024;
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keySize);
        return keyPairGenerator.genKeyPair();
    }

    public static byte[] encrypt(PublicKey publicKey, String message) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        return cipher.doFinal(message.getBytes());
    }

    public static byte[] decrypt(PrivateKey privateKey, byte [] encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        return cipher.doFinal(encrypted);
    }
}
