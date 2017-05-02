package com.example.pedro.rsaaes;

import android.util.Base64;
import android.util.Log;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

/**
 * Created by pedro on 21/04/17.
 */

public class CryptoPubKey {

    public byte[] encrypt( String data , String pubKey ) throws Exception
    {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        Cipher cipher = Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding", "BC");
        PublicKey publicKey = strToPublicKey(pubKey);
        cipher.init( Cipher.ENCRYPT_MODE , publicKey );
        // Base 64 encode the encrypted data
        byte[] encryptedBytes = Base64.encode( cipher.doFinal(data.getBytes()), 0 );
        return encryptedBytes;


    }


    public static PublicKey strToPublicKey(String key) throws NoSuchAlgorithmException, InvalidKeySpecException {

        KeyFactory kFactory = KeyFactory.getInstance("RSA", new BouncyCastleProvider());
        // decode base64 of your key
        byte yourKey[] =  Base64.decode(key,0);
        // generate the public key
        X509EncodedKeySpec spec =  new X509EncodedKeySpec(yourKey);
        PublicKey publicKey = (PublicKey) kFactory.generatePublic(spec);
        return publicKey;
    }
}
