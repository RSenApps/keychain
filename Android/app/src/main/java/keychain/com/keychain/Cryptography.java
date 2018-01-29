package keychain.com.keychain;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.io.IOException;
import java.math.BigInteger;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECFieldFp;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.EllipticCurve;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import static keychain.com.keychain.QRScannerActivity.bytesToHex;

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
    /*
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
    */

    public static void buildKeyPair() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore");
        keyPairGenerator.initialize(
                new KeyGenParameterSpec.Builder(
                        "keychain-sign",
                        KeyProperties.PURPOSE_SIGN)
                        .setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1"))
                        .setDigests(KeyProperties.DIGEST_NONE)
                        // Only permit the private key to be used if the user authenticated
                        .setUserAuthenticationRequired(true)
                        .setUserAuthenticationValidityDurationSeconds(10)
                        .build());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
    }


    public static String getPublickey() throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        ECPublicKey ecPublicKey = (ECPublicKey) keyStore.getCertificate("keychain-sign").getPublicKey();

        // test creation
        return bytesToHex(ecPublicKey.getEncoded());

    }


    public static ECPoint getPublicPoint() throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        ECPublicKey ecPublicKey = (ECPublicKey) keyStore.getCertificate("keychain-sign").getPublicKey();

        // test creation
        return ecPublicKey.getW();

    }
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static BigInteger[] extractRSFromSig(byte[] enc) {
        if(enc[0]!=0x30) return null; // bad encoding
        if((enc[1]&0x80)!=0) return null; // unsupported length encoding
        if(enc[2]!=0x02) return null; // bad encoding
        if((enc[3]&0x80)!=0) return null; // unsupported length encoding
        int rLength = enc[3];
        byte[] rArr = new byte[rLength];
        System.arraycopy(enc, 4, rArr, 0, rLength);
        BigInteger r = new BigInteger(rArr);
        if(enc[4+rLength]!=0x02) return null; // bad encoding
        if((enc[5+rLength]&0x80)!=0) return null; // unsupported length encoding
        int sLength = enc[5+rLength];
        byte[] sArr = new byte[sLength];
        System.arraycopy(enc, 6+rLength, sArr, 0, sLength);
        BigInteger s = new BigInteger(sArr);
        return new BigInteger[] {r, s};
    }

    public static BigInteger[] sign(String message) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableEntryException, InvalidKeyException, SignatureException {
        KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
        ks.load(null);
        KeyStore.Entry entry = ks.getEntry("keychain-sign", null);

        Signature signature = Signature.getInstance("NONEwithECDSA");
        signature.initSign(((KeyStore.PrivateKeyEntry) entry).getPrivateKey());
        signature.update(hexStringToByteArray(message));

        return extractRSFromSig(signature.sign());
    }
}