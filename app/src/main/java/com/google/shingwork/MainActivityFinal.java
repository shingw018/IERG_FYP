package com.google.shingwork;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.users.FullAccount;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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

import java.util.Base64;

public class MainActivityFinal extends AppCompatActivity {
    // Global Variables
    private DriveServiceHelperFinal driveServiceHelper;
    // Buttons
    private Button btn_encrypt;
    private Button btn_decrypt;
    private Button btn_addReceiver;
    private Button btn_myPublicKey;
    private Button btn_dropbox;
    // Files Global
    private File FILESelected;
    private File FILEEncrypted;
    private String FILEDownloadedPath;
    // password Global
    private String PPassword;
    // final variables
    final static String folderRoot = "IERG-FYP";
    final static String folderKeys = "ECC-KEYS";
    final static String folderReceivers = "RECEIVERS";
    final static String folderSelected = "SELECTED";
    final static String folderEncrypted = "ENCRYPTED";
    final static String folderDecrypted = "DECRYPTED";
    final static String folderDownloaded = "DOWNLOADED";
    final static String folderECCEncrypted = "ECC-ENCRYPTED";
    final static String folderECCDecrypted = "ECC-DECRYPTED";
    final static String filePublic = "MyPublicKey.txt";
    final static String filePrivate = "MyPrivate.txt";
    private String SFileFormat = "";
    private String SFileName = "";
    // Requests Code
    final int RCSelectFileFromLocal = 10;
    final int RCSelectFileFromDrive = 20;
    final int RCAddReceiver = 40;
    final int RCDropboxLogin = 50;
    // Mode
    String MMode = "";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // the inheritance
        super.onCreate(savedInstanceState);
        // layout
        setContentView(R.layout.activity_main_final);
        // set listeners
        setListeners();
        //Google sign in
        requestSignIn();
    }

    private void setListeners()
    {
        // set btn_encrypt listener
        btn_encrypt = (Button)findViewById(R.id.btn_encrypt);
        btnEncryptListener();
        // set btn_decrypt listener
        btn_decrypt = (Button)findViewById(R.id.btn_decrypt);
        btnDecryptListener();
        // set btn_addReceiver listener
        btn_addReceiver = (Button)findViewById(R.id.btn_addReceiver);
        btnAddReceiverListener();
        // set btn_myPublicKey listener
        btn_myPublicKey = (Button)findViewById(R.id.btn_myPublicKey);
        btnMyPublicKeyListener();
        btn_dropbox = (Button) findViewById(R.id.btn_dropbox);
        btnDropboxListener();
    }

    private void requestSignIn()
    {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .build();

        GoogleSignInClient client = GoogleSignIn.getClient(MainActivityFinal.this, signInOptions);

        startActivityForResult(client.getSignInIntent(),400);
    }

    private void btnEncryptListener()
    {
        btn_encrypt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
               MMode = "ENCRYPTMODE";
               dialogMode();
            }
        });
    }

    private void btnDecryptListener()
    {
        btn_decrypt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MMode = "DECRYPTMODE";
                // 1 for decrypt
                dialogMode();
            }
        });
    }

    private void btnAddReceiverListener()
    {
        btn_addReceiver.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // show a new Intent Page
                // with Scanner and button for image
                // read the QR code and write to a file and store it inside a folder
                addReceiver();
            }
        });
    }

    private void btnMyPublicKeyListener()
    {
        btn_myPublicKey.setOnClickListener(new View.OnClickListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v)
            {
                // show the public key
                // generated only once when the app is onCreate
                showMyPublicKey();
            }
        });
    }

    private void btnDropboxListener()
    {
        btn_dropbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dropboxLogin();
            }
        });
    }

    private void dialogMode()
    {
        // set choices
        final String[] items = MMode == "ENCRYPTMODE"
                ? new String[]{"Encrypt for Own Use", "Encrypt for Sharing"}
                : new String[]{"Own files", "Shared files"};
        // your choice
        final int[] yourChoice = {0};
        // title
        final String title = MMode == "ENCRYPTMODE" ? "Encryption" : "Decryption";
        // dialog
        AlertDialog.Builder choiceDialog = new AlertDialog.Builder(MainActivityFinal.this);
        choiceDialog.setTitle("Choose for " + title + " type: ");
        // set onClick
        choiceDialog.setSingleChoiceItems(items, 0,
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        yourChoice[0] = which;
                    }
                });
        // show response
        choiceDialog.setPositiveButton("Select File",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // show message
                        //Toast.makeText(MainActivityFinal.this, "Chosen for " + items[yourChoice[0]], Toast.LENGTH_SHORT).show();
                        // perform Actions
                        performActions((MMode == "ENCRYPTMODE" ? "1" : "2") + String.valueOf(yourChoice[0]));
                    }
                });
        choiceDialog.show();
    }

    private void addReceiver()
    {
        Intent intent = new Intent(MainActivityFinal.this, ScanActivity.class);
        startActivityForResult(intent, RCAddReceiver);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showMyPublicKey(){
        try {
            // Read Public Key File
            File file = new File(Environment.getExternalStorageDirectory(), folderRoot + "/" + folderKeys + "/" + filePublic);
            // create FileInputStream object
            FileInputStream fin = new FileInputStream(file);
            byte[] fileContent = new byte[(int)file.length()];
            // Reads up to certain bytes of data from this input stream into an array of bytes.
            fin.read(fileContent);

            //byte[] bytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));

            Intent intent = new Intent(MainActivityFinal.this, MyPublicKey.class);
            intent.putExtra("myPublicKey", new String(fileContent));
            intent.putExtra("myPublicKeyFilePath", file.getAbsoluteFile().toString());
            startActivity(intent);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void performActions(String choice)
    {
        switch (choice)
        {
            // 1 for encrypt
            // encrypt own files
            case "10":
                MMode = "ENCRYPTMODEOWN";
                selectFileFromLocal();
                break;
            // encrypt shared files
            case "11":
                MMode = "ENCRYPTMODESHARING";
                selectFileFromDrive();
                break;
            // 2 for decrypt
            // decrypt own files
            case "20":
                MMode = "DECRYPTMODEOWN";
                selectFileFromDrive();
                break;
            // decrypt shared files
            case "21":
                MMode = "DECRYPTMODSHARING";
                selectFileFromLocal();
                break;
        }
    }

    private void selectFileFromLocal()
    {
        Intent filePicker = new Intent(Intent.ACTION_GET_CONTENT);
        filePicker.setType("*/*");
        filePicker = Intent.createChooser(filePicker, "Choose a file");
        startActivityForResult(filePicker, RCSelectFileFromLocal);
    }

    private void selectFileFromDrive()
    {
        driveServiceHelper.retrieve().addOnSuccessListener(new OnSuccessListener<HashMap<String, String>>() {
            @Override
            public void onSuccess(HashMap<String, String> driveFileList) {
                Intent intent = new Intent(getApplicationContext(), MainActivity2.class);
                intent.putExtra("driveFileList", driveFileList);
                startActivityForResult(intent, RCSelectFileFromDrive);
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Cannot get the Drive File List", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
            // request code = 400 => Google Sign-in Intent
            case 400:
                if(resultCode == RESULT_OK)
                {
                    handleSignInIntent(data);
                    // permission check
                    permissionCheck();
                }
                break;
            case RCSelectFileFromLocal:
                if(resultCode == RESULT_OK)
                {
                    dialogFileName(data);
                }
                break;
            case RCSelectFileFromDrive:
                if(resultCode == RESULT_OK)
                {
                    // go to DriveServiceHelper to download the file with the file id retrieved from the list
                    downloadFile(data.getStringExtra("downloadFileId"), data.getStringExtra("downloadFileName"));
                }
                break;
            case RCAddReceiver:
                if(resultCode == RESULT_OK)
                {
                    saveReceiverPublicKey(data.getByteArrayExtra("receiverPublicKey"), data.getStringExtra("receiverName"));
                }
                break;
            case RCDropboxLogin:
                if(resultCode == RESULT_OK)
                {
                    // set global variable of dropbox access token
                    SharedPreferences sharedPreferences = getSharedPreferences("com.google.shingwork", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("dropbox_access_token", data.getStringExtra("dropboxAccessToken"));
                    editor.apply();
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
                                .usingOAuth2(MainActivityFinal.this, Collections.singleton(DriveScopes.DRIVE_FILE));

                        credential.setSelectedAccount(googleSignInAccount.getAccount());

                        Drive googleDriveService = new Drive.Builder(
                                new NetHttpTransport(),
                                new GsonFactory(),
                                credential)
                                .setApplicationName("ShingWork")
                                .build();

                        driveServiceHelper = new DriveServiceHelperFinal(googleDriveService);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Cannot Log in to Google", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    protected void permissionCheck() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        }
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
        }
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            //When permission not granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 3);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Check condition
        if((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED))
        {
            //When permission granted
            //Check condition
            if(requestCode == 1)
            {
                //Toast.makeText(getApplicationContext(), "Permission WRITE TO EXTERNAL STORAGE ALLOWED", Toast.LENGTH_SHORT).show();
                permissionCheck();
                // generate my public key
                generateMyPublicKey();
            } else if (requestCode == 2)
            {
                //Toast.makeText(getApplicationContext(), "CAMERA ALLOWED", Toast.LENGTH_SHORT).show();
                permissionCheck();
            } else
            {
                //Toast.makeText(getApplicationContext(), "Permission READ TO EXTERNAL STORAGE ALLOWED", Toast.LENGTH_SHORT).show();
                permissionCheck();
                // generate my public key
                generateMyPublicKey();
            }
        } else
        {
            //When perimission denied
            //Display toast
            //Toast.makeText(getApplicationContext(), "READ/WRITE To EXTERNAL STORAGE Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    // Part for download and decrypt
    private void downloadFile(String fileid, String filename)
    {
        driveServiceHelper.downloadFile(fileid,filename).addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String filepath) {
                FILEDownloadedPath = filepath;
                //Toast.makeText(getApplicationContext(), "Downloaded Successfully", Toast.LENGTH_LONG).show();
                if (MMode == "DECRYPTMODEOWN")
                {
                    // show dialog for input password and store the password in PPassword
                    dialogPassword();
                } else {
                    selectReceiverPublicKey();
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Downloaded failure", Toast.LENGTH_LONG).show();
                        Log.e("MainActivityFinal", "Error in MainActivity in download!", e);
                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void generateMyPublicKey()
    {
        if(!new File(Environment.getExternalStorageDirectory(), folderRoot + "/" + folderKeys + "/" + filePublic).exists()
                && !new File(Environment.getExternalStorageDirectory(), folderRoot + "/" + folderKeys + "/" + filePrivate).exists())
        {
            try
            {
                // generate ecc keys
                KeyPair keyPair = generateECKeys();
                byte[] publicKey = keyPair.getPublic().getEncoded();
                byte[] privateKey = keyPair.getPrivate().getEncoded();

                // write files to store the keys
                File root = new File(Environment.getExternalStorageDirectory(), folderRoot + "/" + folderKeys);
                if(!root.exists())
                {
                    root.mkdirs();
                }

                File filePublicKey = new File(root, filePublic);
                filePublicKey.createNewFile();

                // writing the keys to the files
                FileOutputStream fileOutputStreamPublicKey = new FileOutputStream(filePublicKey.getAbsolutePath());

                String s = Base64.getEncoder().encodeToString(publicKey);

                fileOutputStreamPublicKey.write(s.getBytes());
                fileOutputStreamPublicKey.flush();
                fileOutputStreamPublicKey.close();

                // saving the private key to the shared preference
                SharedPreferences sharedPreferences = getSharedPreferences("com.google.shingwork", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("private_key", Base64.getEncoder().encodeToString(privateKey));
                editor.apply();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void handleSelectedFile(Intent data)
    {
        try
        {
            // Set Path
            String folder = MMode == "ENCRYPTMODEOWN" ? folderSelected : folderDownloaded;
            File root = new File(Environment.getExternalStorageDirectory(), folderRoot + "/" + folder);
            if (!root.exists()) {
                root.mkdirs();
            }
            File file = new File(root, SFileName + SFileFormat);
            FileWriter writer = new FileWriter(file);
            writer.append("");
            writer.flush();
            writer.close();
            // Convert the read input into a readable path file
            copyInputStreamToFile(getContentResolver().openInputStream(data.getData()), file);
            // Encrypt the file
            if(MMode == "ENCRYPTMODEOWN")
            {
                FILESelected = file;
            } else
            {
                FILEDownloadedPath = file.getAbsolutePath();
            }

            //Toast.makeText(getApplicationContext(),"Selected: " + file.getAbsolutePath(),Toast.LENGTH_SHORT).show();
            if(MMode == "ENCRYPTMODEOWN")
            {
                dialogPassword();
            } else
            {
                selectReceiverPublicKey();
            }
        } catch (IOException e)
        {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Cannot read file (IERG-FYP/Encrypted)", Toast.LENGTH_SHORT).show();
        } catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Cannot select file and encrypt file", Toast.LENGTH_SHORT).show();
        }
    }

    // Creating the file through inputstream data
    private static void copyInputStreamToFile(InputStream inputStream, File file)
    {
        // append = false
        try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
            int read;
            byte[] bytes = new byte[inputStream.available()];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void dialogPassword()
    {
        // show dialog for inputting the password
        final EditText editText = new EditText(MainActivityFinal.this);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(MainActivityFinal.this);
        String titleMode = MMode == "ENCRYPTMODEOWN" ? "encryption" : "decryption";
        inputDialog.setTitle("Enter a password for " + titleMode).setView(editText);
        String buttonText = MMode == "ENCRYPTMODEOWN" ? "Encrypt and Upload" : "Decrypt";
        inputDialog.setPositiveButton(buttonText,
                new DialogInterface.OnClickListener()
                {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        //Toast.makeText(MainActivityFinal.this, editText.getText().toString(), Toast.LENGTH_SHORT).show();
                        // password
                        PPassword = editText.getText().toString();
                        if (MMode == "ENCRYPTMODEOWN")
                        {
                            // encrypt file
                            encrypt();
                            // upload file
                            uploadFile();
                        } else
                        {
                            //Log.d("Mode", "now decrypt");
                            decrypt();
                        }

                    }
                }).show();
    }

    // Part for Encrypting and Decrypting
    // Creating SALT
    private static byte[] getSalt()
    {
        byte[] salt = new byte[16];
        try
        {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.nextBytes(salt);
        } catch (Exception NoSuchAlgorithmException)
        {
            NoSuchAlgorithmException.printStackTrace();
        }
        return salt;
    }

    // Encrypting the file
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void encrypt()
    {
        try
        {
            File file = FILESelected;
            final String password = PPassword;
            byte[] salt = getSalt();
            final int iterations = 1000;
            final int outputKeyLength = 256;

            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");;
            KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, iterations, outputKeyLength);
            SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);

            // create FileInputStream object
            FileInputStream fin = new FileInputStream(file);

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
            byte[] combine = new byte[cipherText.length + salt.length];
            System.arraycopy(salt, 0, combine, 0, salt.length);
            System.arraycopy(cipherText, 0, combine, salt.length, cipherText.length);

            // Set Path
            File root = new File(Environment.getExternalStorageDirectory(), folderRoot + "/" + folderEncrypted);
            if (!root.exists()) {
                root.mkdirs();
            }
            File outputFile = new File(root, file.getName());
            FILEEncrypted = outputFile;

            OutputStream outputStream = new FileOutputStream(outputFile);
            outputStream.write(combine);
            outputStream.flush();
            outputStream.close();

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }








    // testing
    // Encrypting the file
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void testencrypt(View view)
    {
        try
        {
            File file = new File("/storage/emulated/0/IERG-FYP/100mb.txt");
            final String password = "test";
            byte[] salt = getSalt();
            final int iterations = 1000;
            final int outputKeyLength = 256;

            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");;
            KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, iterations, outputKeyLength);
            SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);

            // create FileInputStream object
            FileInputStream fin = new FileInputStream(file);

            byte[] fileContent = new byte[(int)file.length()];

            // Reads up to certain bytes of data from this input stream into an array of bytes.
            fin.read(fileContent);

            long startTime = System.nanoTime();

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
            byte[] combine = new byte[cipherText.length + salt.length];
            System.arraycopy(salt, 0, combine, 0, salt.length);
            System.arraycopy(cipherText, 0, combine, salt.length, cipherText.length);


            long endTime = System.nanoTime();
            long timeElasped = endTime-startTime;
            Log.d("tested time", String.valueOf(timeElasped/1000000));
            Log.d("test time nano", String.valueOf(timeElasped));

            // Set Path
            File root = new File("/storage/emulated/0/IERG-FYP/Eencrypted100mb.txt");

            OutputStream outputStream = new FileOutputStream(root);
            outputStream.write(combine);
            outputStream.flush();
            outputStream.close();

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }





    public void testdecrypt(View view)
    {
        try
        {
            long startTime = System.nanoTime();
            byte[] nonceBytes = new byte[12];

            // Get Cipher Instance
            Cipher cipher = Cipher.getInstance("ChaCha20/Poly1305/NoPadding");

            // Create IvParamterSpec
            AlgorithmParameterSpec ivParameterSpec = new IvParameterSpec(nonceBytes);

            java.io.File file = new java.io.File("/storage/emulated/0/IERG-FYP/encrypted102.4kb-1.txt");

            // create FileInputStream object
            FileInputStream fin = new FileInputStream(file);

            byte[] salt = new byte[16];

            // Reads up to certain bytes of data from this input stream into an array of bytes.
            fin.read(salt, 0, 16);

            // Initialize Cipher for DECRYPT_MODE
            final int iterations = 1000;
            final int outputKeyLength = 256;

            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec keySpec = new PBEKeySpec("test".toCharArray(), salt, iterations, outputKeyLength);
            SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
            SecretKeySpec keySpec2 = new SecretKeySpec(secretKey.getEncoded(), "ChaCha20");

            cipher.init(Cipher.DECRYPT_MODE, keySpec2, ivParameterSpec);

            int len = (int)file.length() - 16;
            byte[] fileContent = new byte[len];

            // Reads up to certain bytes of data from this input stream into an array of bytes.
            fin.read(fileContent, 0, len);

            // Perform Decryption
            byte[] decryptedText = cipher.doFinal(fileContent);

            long endTime = System.nanoTime();
            long timeElapsed = endTime - startTime;
            Log.d("Decryption time", String.valueOf(timeElapsed/1000000));
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    @SuppressLint("LongLogTag")
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void testeccEncrypt(View view) {

        try {
            long startTime = System.nanoTime();
            // read file to encrypt
            File fileToEncrypt = new File("/storage/emulated/0/IERG-FYP/encrypted102.4kb-1.txt");
            FileInputStream fileInputStreamToEncrypt = null;
            fileInputStreamToEncrypt = new FileInputStream(fileToEncrypt);
            byte[] bytesToEncrypt = new byte[(int)fileToEncrypt.length()];
            fileInputStreamToEncrypt.read(bytesToEncrypt);

            // public key
            File filePublicKey = new File("/storage/emulated/0/IERG-FYP/ECC-KEYS/MyPublicKey.txt");

            byte[] bytesPublicKey = new byte[(int)filePublicKey.length()];
            FileInputStream fileInputStreamPublicKey = null;
            fileInputStreamPublicKey = new FileInputStream(filePublicKey);
            fileInputStreamPublicKey.read(bytesPublicKey);

            String s = new String(bytesPublicKey);
            byte[] b = Base64.getDecoder().decode(s);

            // Key agreement
            KeyFactory kfPublic = KeyFactory.getInstance("EC");
            PublicKey publicKey = kfPublic.generatePublic(new X509EncodedKeySpec(b));

            // private key
            SharedPreferences sharedPreferences = getSharedPreferences("com.google.shingwork", Context.MODE_PRIVATE);
            byte[] bytesPrivateKey = Base64.getDecoder().decode(sharedPreferences.getString("private_key", ""));

            //Key agreement
            KeyFactory kfPrivate = KeyFactory.getInstance("EC");
            PrivateKey privateKey = kfPrivate.generatePrivate(new PKCS8EncodedKeySpec(bytesPrivateKey));

            KeyAgreement ka = KeyAgreement.getInstance("ECDH");
            ka.init(privateKey);
            ka.doPhase(publicKey, true);
            byte[] key = ka.generateSecret();

            byte[] nonceBytes = new byte[12];
            Cipher cipher = Cipher.getInstance("ChaCha20/Poly1305/NoPadding");

            // Create IvParamterSpec
            AlgorithmParameterSpec ivParameterSpec = new IvParameterSpec(nonceBytes);

            // Create SecretKeySpec
            SecretKeySpec keySpec2 = new SecretKeySpec(key, "ChaCha20");

            // Initialize Cipher for ENCRYPT_MODE
            cipher.init(Cipher.ENCRYPT_MODE, keySpec2, ivParameterSpec);

            // Perform Encryption
            byte[] cipherText = cipher.doFinal(bytesToEncrypt);

            long endTime = System.nanoTime();
            long timeElasped = endTime - startTime;

            Log.d("ECC encrypted time", String.valueOf(timeElasped/1000000));

            File outputRoot = new File("/storage/emulated/0/IERG-FYP/Eeccencrypted102.4kb.txt");
            outputRoot.createNewFile();

            OutputStream outputStream = new FileOutputStream(outputRoot);
            outputStream.write(cipherText);
            outputStream.flush();
            outputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
            noSuchAlgorithmException.printStackTrace();
        } catch (InvalidKeySpecException invalidKeySpecException) {
            invalidKeySpecException.printStackTrace();
        } catch (InvalidKeyException invalidKeyException) {
            invalidKeyException.printStackTrace();
        } catch (NoSuchPaddingException noSuchPaddingException) {
            noSuchPaddingException.printStackTrace();
        } catch (InvalidAlgorithmParameterException invalidAlgorithmParameterException) {
            invalidAlgorithmParameterException.printStackTrace();
        } catch (BadPaddingException badPaddingException) {
            badPaddingException.printStackTrace();
        } catch (IllegalBlockSizeException illegalBlockSizeException) {
            illegalBlockSizeException.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void testeccDecrypt(View view)
    {
        try {
            long startTime = System.nanoTime();
            // read file to decrypt
            File fileToDecrypt = new File("/storage/emulated/0/IERG-FYP/eccencrypted102.4kb.txt");
            FileInputStream fileInputStreamToDecrypt = null;
            fileInputStreamToDecrypt = new FileInputStream(fileToDecrypt);
            byte[] bytesToDecrypt = new byte[(int)fileToDecrypt.length()];
            fileInputStreamToDecrypt.read(bytesToDecrypt);

            // public key
            File filePublicKey = new File("/storage/emulated/0/IERG-FYP/ECC-KEYS/MyPublicKey.txt");

            byte[] bytesPublicKey = new byte[(int)filePublicKey.length()];
            FileInputStream fileInputStreamPublicKey = null;
            fileInputStreamPublicKey = new FileInputStream(filePublicKey);
            fileInputStreamPublicKey.read(bytesPublicKey);

            String s = new String(bytesPublicKey);
            byte[]  b = Base64.getDecoder().decode(s);

            // Key agreement
            KeyFactory kfPublic = KeyFactory.getInstance("EC");
            PublicKey publicKey = kfPublic.generatePublic(new X509EncodedKeySpec(b));

            // private key
            SharedPreferences sharedPreferences = getSharedPreferences("com.google.shingwork", Context.MODE_PRIVATE);
            byte[] bytesPrivateKey = Base64.getDecoder().decode(sharedPreferences.getString("private_key", ""));

            //Key agreement
            KeyFactory kfPrivate = KeyFactory.getInstance("EC");
            PrivateKey privateKey = kfPrivate.generatePrivate(new PKCS8EncodedKeySpec(bytesPrivateKey));

            KeyAgreement ka = KeyAgreement.getInstance("ECDH");
            ka.init(privateKey);
            ka.doPhase(publicKey, true);
            byte[] key = ka.generateSecret();

            byte[] nonceBytes = new byte[12];
            Cipher cipher = Cipher.getInstance("ChaCha20/Poly1305/NoPadding");

            // Create IvParamterSpec
            AlgorithmParameterSpec ivParameterSpec = new IvParameterSpec(nonceBytes);

            // Create SecretKeySpec
            SecretKeySpec keySpec2 = new SecretKeySpec(key, "ChaCha20");

            // Initialize Cipher for ENCRYPT_MODE
            cipher.init(Cipher.DECRYPT_MODE, keySpec2, ivParameterSpec);

            // Perform Encryption
            byte[] cipherText = cipher.doFinal(bytesToDecrypt);

            long endTime = System.nanoTime();
            long timeElasped = endTime-startTime;
            Log.d("ecc decrypt time", String.valueOf(timeElasped/1000000));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
            noSuchAlgorithmException.printStackTrace();
        } catch (InvalidKeySpecException invalidKeySpecException) {
            invalidKeySpecException.printStackTrace();
        } catch (InvalidKeyException invalidKeyException) {
            invalidKeyException.printStackTrace();
        } catch (NoSuchPaddingException noSuchPaddingException) {
            noSuchPaddingException.printStackTrace();
        } catch (InvalidAlgorithmParameterException invalidAlgorithmParameterException) {
            invalidAlgorithmParameterException.printStackTrace();
        } catch (BadPaddingException badPaddingException) {
            badPaddingException.printStackTrace();
        } catch (IllegalBlockSizeException illegalBlockSizeException) {
            illegalBlockSizeException.printStackTrace();
        }
    }
































    @RequiresApi(api = Build.VERSION_CODES.O)
    private void uploadFile() {
        // uploading file to drive
        driveServiceHelper.uploadFile(FILEEncrypted).addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                Toast.makeText(getApplicationContext(), "File Encrypted and Uploaded Successfully", Toast.LENGTH_LONG).show();
                // backup to dropbox
                dropboxBackup();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failure: Cannot encrypted and upload file", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void decrypt()
    {
        try
        {
            byte[] nonceBytes = new byte[12];

            // Get Cipher Instance
            Cipher cipher = Cipher.getInstance("ChaCha20/Poly1305/NoPadding");

            // Create IvParamterSpec
            AlgorithmParameterSpec ivParameterSpec = new IvParameterSpec(nonceBytes);

            java.io.File file = new java.io.File(FILEDownloadedPath);

            // create FileInputStream object
            FileInputStream fin = new FileInputStream(file);

            byte[] salt = new byte[16];

            // Reads up to certain bytes of data from this input stream into an array of bytes.
            fin.read(salt, 0, 16);

            // Initialize Cipher for DECRYPT_MODE
            final int iterations = 1000;
            final int outputKeyLength = 256;

            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec keySpec = new PBEKeySpec(PPassword.toCharArray(), salt, iterations, outputKeyLength);
            SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
            SecretKeySpec keySpec2 = new SecretKeySpec(secretKey.getEncoded(), "ChaCha20");

            cipher.init(Cipher.DECRYPT_MODE, keySpec2, ivParameterSpec);

            int len = (int)file.length() - 16;
            byte[] fileContent = new byte[len];

            // Reads up to certain bytes of data from this input stream into an array of bytes.
            fin.read(fileContent, 0, len);

            // Perform Decryption
            byte[] decryptedText = cipher.doFinal(fileContent);

            File root = new File(Environment.getExternalStorageDirectory(), folderRoot + "/" + folderDecrypted);
            if (!root.exists()) {
                root.mkdirs();
            }

            String[] splitArray = FILEDownloadedPath.split("/");
            File outputFile = new File(root, splitArray[splitArray.length - 1]);

            OutputStream outputStream = new FileOutputStream(outputFile);
            outputStream.write(decryptedText);
            outputStream.flush();
            outputStream.close();

            Toast.makeText(getApplicationContext(), "Decrypted Successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void selectReceiverPublicKey()
    {
        // select receiver public key
        // Use array from resources for client to select
        String path = Environment.getExternalStorageDirectory().toString()+"/" + folderRoot+"/"+folderReceivers;
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: "+ files.length);
        //String [] fileNames = {};
        List<String> fileNamesList = new ArrayList<String>();
        for (int i = 0; i < files.length; i++)
        {
            Log.d("Files", "FileName:" + files[i].getName());
            fileNamesList.add(files[i].getName());
        }
        String [] fileNames = fileNamesList.toArray(new String[0]);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivityFinal.this);
        String title = MMode == "ENCRYPTMODESHARING" ? "receiver" : "sender";
        builder.setTitle("Pick the public key of the " + title);

        builder.setItems(fileNames, new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // ECC encrypt or ECC decrypt
                String path = Environment.getExternalStorageDirectory().toString() + "/" + folderRoot + "/" + folderReceivers + "/";
                if (MMode == "ENCRYPTMODESHARING")
                {
                    eccEncrypt(path + fileNames[which]);
                } else
                {
                    eccDecrypt(path + fileNames[which]);
                }
            }
        });

        builder.show();
    }

    @SuppressLint("LongLogTag")
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void eccEncrypt(String receiverPublicKey) {

        Log.d("receiverPublickey get in function", receiverPublicKey);

        try {
            // read file to encrypt
            File fileToEncrypt = new File(FILEDownloadedPath);
            FileInputStream fileInputStreamToEncrypt = null;
            fileInputStreamToEncrypt = new FileInputStream(fileToEncrypt);
            byte[] bytesToEncrypt = new byte[(int)fileToEncrypt.length()];
            fileInputStreamToEncrypt.read(bytesToEncrypt);

            // public key
            File filePublicKey = new File(receiverPublicKey);

            byte[] bytesPublicKey = new byte[(int)filePublicKey.length()];
            FileInputStream fileInputStreamPublicKey = null;
            fileInputStreamPublicKey = new FileInputStream(filePublicKey);
            fileInputStreamPublicKey.read(bytesPublicKey);

            String s = new String(bytesPublicKey);
            byte[] b = Base64.getDecoder().decode(s);

            // Key agreement
            KeyFactory kfPublic = KeyFactory.getInstance("EC");
            PublicKey publicKey = kfPublic.generatePublic(new X509EncodedKeySpec(b));

            // private key
            SharedPreferences sharedPreferences = getSharedPreferences("com.google.shingwork", Context.MODE_PRIVATE);
            byte[] bytesPrivateKey = Base64.getDecoder().decode(sharedPreferences.getString("private_key", ""));

            //Key agreement
            KeyFactory kfPrivate = KeyFactory.getInstance("EC");
            PrivateKey privateKey = kfPrivate.generatePrivate(new PKCS8EncodedKeySpec(bytesPrivateKey));

            KeyAgreement ka = KeyAgreement.getInstance("ECDH");
            ka.init(privateKey);
            ka.doPhase(publicKey, true);
            byte[] key = ka.generateSecret();

            byte[] nonceBytes = new byte[12];
            Cipher cipher = Cipher.getInstance("ChaCha20/Poly1305/NoPadding");

            // Create IvParamterSpec
            AlgorithmParameterSpec ivParameterSpec = new IvParameterSpec(nonceBytes);

            // Create SecretKeySpec
            SecretKeySpec keySpec2 = new SecretKeySpec(key, "ChaCha20");

            // Initialize Cipher for ENCRYPT_MODE
            cipher.init(Cipher.ENCRYPT_MODE, keySpec2, ivParameterSpec);

            // Perform Encryption
            byte[] cipherText = cipher.doFinal(bytesToEncrypt);

            File outputRoot = new File(Environment.getExternalStorageDirectory(), folderRoot + "/" + folderECCEncrypted);
            if (!outputRoot.exists())
            {
                outputRoot.mkdirs();
            }

            String[] splitResult = FILEDownloadedPath.split(("/"));

            File outputFile = new File(outputRoot, "ECC-ENCRYPT-" + splitResult[splitResult.length - 1]);
            outputFile.createNewFile();

            OutputStream outputStream = new FileOutputStream(outputFile);
            outputStream.write(cipherText);
            outputStream.flush();
            outputStream.close();

            FILEEncrypted = outputFile;

            uploadFile();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
            noSuchAlgorithmException.printStackTrace();
        } catch (InvalidKeySpecException invalidKeySpecException) {
            invalidKeySpecException.printStackTrace();
        } catch (InvalidKeyException invalidKeyException) {
            invalidKeyException.printStackTrace();
        } catch (NoSuchPaddingException noSuchPaddingException) {
            noSuchPaddingException.printStackTrace();
        } catch (InvalidAlgorithmParameterException invalidAlgorithmParameterException) {
            invalidAlgorithmParameterException.printStackTrace();
        } catch (BadPaddingException badPaddingException) {
            badPaddingException.printStackTrace();
        } catch (IllegalBlockSizeException illegalBlockSizeException) {
            illegalBlockSizeException.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void eccDecrypt(String receiverPublicKey)
    {
        try {
            // read file to decrypt
            File fileToDecrypt = new File(FILEDownloadedPath);
            FileInputStream fileInputStreamToDecrypt = null;
            fileInputStreamToDecrypt = new FileInputStream(fileToDecrypt);
            byte[] bytesToDecrypt = new byte[(int)fileToDecrypt.length()];
            fileInputStreamToDecrypt.read(bytesToDecrypt);

            // public key
            File filePublicKey = new File(receiverPublicKey);

            byte[] bytesPublicKey = new byte[(int)filePublicKey.length()];
            FileInputStream fileInputStreamPublicKey = null;
            fileInputStreamPublicKey = new FileInputStream(filePublicKey);
            fileInputStreamPublicKey.read(bytesPublicKey);

            String s = new String(bytesPublicKey);
            byte[]  b = Base64.getDecoder().decode(s);

            // Key agreement
            KeyFactory kfPublic = KeyFactory.getInstance("EC");
            PublicKey publicKey = kfPublic.generatePublic(new X509EncodedKeySpec(b));

            // private key
            SharedPreferences sharedPreferences = getSharedPreferences("com.google.shingwork", Context.MODE_PRIVATE);
            byte[] bytesPrivateKey = Base64.getDecoder().decode(sharedPreferences.getString("private_key", ""));

            //Key agreement
            KeyFactory kfPrivate = KeyFactory.getInstance("EC");
            PrivateKey privateKey = kfPrivate.generatePrivate(new PKCS8EncodedKeySpec(bytesPrivateKey));

            KeyAgreement ka = KeyAgreement.getInstance("ECDH");
            ka.init(privateKey);
            ka.doPhase(publicKey, true);
            byte[] key = ka.generateSecret();

            byte[] nonceBytes = new byte[12];
            Cipher cipher = Cipher.getInstance("ChaCha20/Poly1305/NoPadding");

            // Create IvParamterSpec
            AlgorithmParameterSpec ivParameterSpec = new IvParameterSpec(nonceBytes);

            // Create SecretKeySpec
            SecretKeySpec keySpec2 = new SecretKeySpec(key, "ChaCha20");

            // Initialize Cipher for ENCRYPT_MODE
            cipher.init(Cipher.DECRYPT_MODE, keySpec2, ivParameterSpec);

            // Perform Encryption
            byte[] cipherText = cipher.doFinal(bytesToDecrypt);

            File root = new File(Environment.getExternalStorageDirectory(), folderRoot + "/" + folderECCDecrypted);
            if(!root.exists())
            {
                root.mkdirs();
            }

            String[] splitResult = FILEDownloadedPath.split(("/"));

            File outputFile = new File(root, "ECC-DECRYPT-" + splitResult[splitResult.length - 1]);

            OutputStream outputStream = new FileOutputStream(outputFile);
            outputStream.write(cipherText);
            outputStream.flush();
            outputStream.close();

            FILEDownloadedPath = outputFile.getAbsolutePath();

            dialogPassword();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
            noSuchAlgorithmException.printStackTrace();
        } catch (InvalidKeySpecException invalidKeySpecException) {
            invalidKeySpecException.printStackTrace();
        } catch (InvalidKeyException invalidKeyException) {
            invalidKeyException.printStackTrace();
        } catch (NoSuchPaddingException noSuchPaddingException) {
            noSuchPaddingException.printStackTrace();
        } catch (InvalidAlgorithmParameterException invalidAlgorithmParameterException) {
            invalidAlgorithmParameterException.printStackTrace();
        } catch (BadPaddingException badPaddingException) {
            badPaddingException.printStackTrace();
        } catch (IllegalBlockSizeException illegalBlockSizeException) {
            illegalBlockSizeException.printStackTrace();
        }
    }

    private void saveReceiverPublicKey(byte[] receiverPublicKey, String receiverName)
    {
        try
        {
            File root = new File(Environment.getExternalStorageDirectory(), folderRoot + "/" + folderReceivers);
            if(!root.exists())
            {
                root.mkdirs();
            }
            File fileReceiver = new File(root, receiverName + ".txt");
            fileReceiver.createNewFile();

            OutputStream outputStreamReceiver = new FileOutputStream(fileReceiver.getAbsolutePath());
            outputStreamReceiver.write(receiverPublicKey);
            outputStreamReceiver.flush();
            outputStreamReceiver.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static KeyPair generateECKeys() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
            kpg.initialize(256);
            KeyPair kp = kpg.generateKeyPair();

            return kp;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void dialogFileFormat(Intent data) {
        String [] fileFormats = MainActivityFinal.this.getResources().getStringArray(R.array.fileFormats);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivityFinal.this);
        builder.setTitle("Choose the format of the selected file");

        builder.setItems(fileFormats, new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SFileFormat = fileFormats[which];
                handleSelectedFile(data);
            }
        });

        builder.show();
    }

    private void dialogFileName(Intent data)
    {
        // show dialog for inputting the password
        final EditText editText = new EditText(MainActivityFinal.this);
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(MainActivityFinal.this);
        inputDialog.setTitle("Set a name for the selected file").setView(editText);
        inputDialog.setPositiveButton("Next",
                new DialogInterface.OnClickListener()
                {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // file name
                        SFileName = editText.getText().toString();
                        dialogFileFormat(data);
                    }
                }).show();
    }

    private void dropboxLogin()
    {
        Intent intent = new Intent(getApplicationContext(), MainActivityDropbox.class);
        startActivityForResult(intent, RCDropboxLogin);
    }

    private void dropboxBackup() {
        new Thread(runnable).start();
    }

    Runnable runnable = new Runnable(){
        @Override
        public void run() {
            try {
                DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
                // get dropbox access token
                SharedPreferences sharedPreferences = getSharedPreferences("com.google.shingwork", Context.MODE_PRIVATE);

                DbxClientV2 client = new DbxClientV2(config, sharedPreferences.getString("dropbox_access_token", ""));

                FullAccount account = null;

                account = client.users().getCurrentAccount();

                Log.d("Dropbox user name", account.getName().getDisplayName());

                // create FileInputStream object
                FileInputStream fin = null;

                fin = new FileInputStream(FILEEncrypted);


                FileMetadata metadata = client.files().uploadBuilder("/Backup/" + FILEEncrypted.getName())
                        .uploadAndFinish(fin);
            }
             catch (DbxException e) {

                //Toast.makeText(MainActivityFinal.this, "Fail with dropbox upload", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {

                //Toast.makeText(MainActivityFinal.this, "Fail with dropbox upload", Toast.LENGTH_SHORT).show();
            } catch(Exception e) {

                //Toast.makeText(MainActivityFinal.this, "Fail with dropbox upload", Toast.LENGTH_SHORT).show();
            }
        }
    };
}