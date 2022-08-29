package com.google.shingwork;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;


public class MainActivity extends AppCompatActivity {
    DriveServiceHelper driveServiceHelper;

    public MainActivity() throws NoSuchAlgorithmException {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestSignIn();
    }

    private void requestSignIn()
    {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .build();

        GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);

        startActivityForResult(client.getSignInIntent(), 400);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
            case 400:
                if(resultCode == RESULT_OK)
                {
                    handleSignInIntent(data);
                }
                break;
        }
    }

    private void handleSignInIntent(Intent data)
    {
        GoogleSignIn.getSignedInAccountFromIntent(data)
                .addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
                    @Override
                    public void onSuccess(GoogleSignInAccount googleSignInAccount) {
                        GoogleAccountCredential credential = GoogleAccountCredential
                                .usingOAuth2(MainActivity.this, Collections.singleton(DriveScopes.DRIVE_FILE));

                        credential.setSelectedAccount(googleSignInAccount.getAccount());

                        Drive googleDriveService = new Drive.Builder(
                                new NetHttpTransport(),
                                new GsonFactory(),
                                credential)
                                .setApplicationName("ShingWork")
                                .build();

                        driveServiceHelper = new DriveServiceHelper(googleDriveService);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    public void uploadPdfFile(View v)
    {
        String filePath = "/storage/emulated/0/ENCRYPTED.jpg";
        driveServiceHelper.createFilePDF(filePath).addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                Toast.makeText(getApplicationContext(), "Uploaded Successfully", Toast.LENGTH_LONG).show();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Upload Failure", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void downloadPdfFile(View view) {
        String fileId = "1sxutstymzDGeJYqfCvLGRx_Zm73pkvuS";
        driveServiceHelper.downFilePDF(fileId).addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                Toast.makeText(getApplicationContext(), "Downloaded Successfully", Toast.LENGTH_LONG).show();
                Log.d("MainActivity", "Error in MainActivity in upload!");
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Downloaded failure", Toast.LENGTH_LONG).show();
                        Log.e("MainActivity", "Error in MainActivity in download!", e);
                    }
                });
    }

    private static byte[] getSalt() throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void encrypt(View view) throws Exception {
        EditText passwd = findViewById(R.id.text);

        final String password = passwd.getText().toString();
        byte[] salt = getSalt();
        final int iterations = 1000;
        final int outputKeyLength = 256;

        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, iterations, outputKeyLength);
        SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);

        java.io.File file = new java.io.File("/storage/emulated/0/那些你很冒險的夢.aac");

        // create FileInputStream object
        FileInputStream fin = null;
        fin = new FileInputStream(file);

        byte[] fileContent = new byte[(int)file.length()];

        // Reads up to certain bytes of data from this input stream into an array of bytes.
        fin.read(fileContent);

        //nouce
        byte[] nonceBytes = new byte[12];

        // Get Cipher Instance
        Cipher cipher = Cipher.getInstance("ChaCha20/Poly1305/NoPadding");

        // Create IvParamterSpec
        AlgorithmParameterSpec ivParameterSpec = new IvParameterSpec(nonceBytes);

        // Create SecretKeySpec
        SecretKeySpec keySpec2 = new SecretKeySpec(secretKey.getEncoded(), "ChaCha20");

        // Initialize Cipher for ENCRYPT_MODE
        cipher.init(Cipher.ENCRYPT_MODE, keySpec2, ivParameterSpec);

        // Perform Encryption
        byte[] cipherText = cipher.doFinal(fileContent);
        //temp = cipherText;

        byte[] combine = new byte[cipherText.length + salt.length];
        System.arraycopy(salt, 0, combine, 0, salt.length);
        System.arraycopy(cipherText, 0, combine, salt.length, cipherText.length);

        OutputStream outputStream = new FileOutputStream("/storage/emulated/0/encrypt.txt");
        outputStream.write(combine);
        outputStream.flush();
        outputStream.close();
    }


    public void Decrypt(View view) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IOException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException {
        byte[] nonceBytes = new byte[12];

        // Get Cipher Instance
        Cipher cipher = Cipher.getInstance("ChaCha20/Poly1305/NoPadding");

        // Create IvParamterSpec
        AlgorithmParameterSpec ivParameterSpec = new IvParameterSpec(nonceBytes);

        java.io.File file = new java.io.File("/storage/emulated/0/encrypt.txt");

        // create FileInputStream object
        FileInputStream fin = null;
        fin = new FileInputStream(file);

        byte[] salt = new byte[16];

        // Reads up to certain bytes of data from this input stream into an array of bytes.
        fin.read(salt, 0, 16);

        // Initialize Cipher for DECRYPT_MODE
        final int iterations = 1000;
        final int outputKeyLength = 256;
        EditText passwd = findViewById(R.id.text2);
        final String password = passwd.getText().toString();

        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, iterations, outputKeyLength);
        SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
        SecretKeySpec keySpec2 = new SecretKeySpec(secretKey.getEncoded(), "ChaCha20");

        cipher.init(Cipher.DECRYPT_MODE, keySpec2, ivParameterSpec);

        int len = (int)file.length() - 16;
        byte[] fileContent = new byte[len];

        // Reads up to certain bytes of data from this input stream into an array of bytes.
        fin.read(fileContent, 0, len);

        // Perform Decryption
        byte[] decryptedText = cipher.doFinal(fileContent);

        OutputStream outputStream = new FileOutputStream("/storage/emulated/0/下載-那些你很冒險的夢.aac");
        outputStream.write(decryptedText);
        outputStream.flush();
        outputStream.close();
    }

    //ECC
    // Generate ephemeral ECDH keypair
    public void ECC () throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(256);
        KeyPair kp = kpg.generateKeyPair();
        byte[] ourPk = kp.getPublic().getEncoded();

        // Display our public key - ourPk
        //console.printf("Public Key: %s%n", printHexBinary(ourPk));

        // Read other's public key: - otherPk
        //byte[] otherPk = parseHexBinary(console.readLine("Other PK: "));

        byte[] otherPk = []
        KeyFactory kf = KeyFactory.getInstance("EC");
        X509EncodedKeySpec pkSpec = new X509EncodedKeySpec(otherPk);
        PublicKey otherPublicKey = kf.generatePublic(pkSpec);

        // Perform key agreement
        KeyAgreement ka = KeyAgreement.getInstance("ECDH");
        ka.init(kp.getPrivate());
        ka.doPhase(otherPublicKey, true);

        // Read shared secret
        byte[] sharedSecret = ka.generateSecret();
        //console.printf("Shared secret: %s%n", printHexBinary(sharedSecret));

        // Derive a key from the shared secret and both public keys
        MessageDigest hash = MessageDigest.getInstance("SHA-256");
        hash.update(sharedSecret);
        // Simple deterministic ordering
        List<ByteBuffer> keys = Arrays.asList(ByteBuffer.wrap(ourPk), ByteBuffer.wrap(otherPk));
        Collections.sort(keys);
        hash.update(keys.get(0));
        hash.update(keys.get(1));

        byte[] derivedKey = hash.digest();
        //console.printf("Final key: %s%n", printHexBinary(derivedKey));
    }


    
}
