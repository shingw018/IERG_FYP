package com.google.shingwork;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class MyPublicKey extends AppCompatActivity {

    private ImageView imageView;
    private TextView publicKeyFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_public_key);
        imageView = (ImageView)findViewById(R.id.publicKeyQRCode);
        publicKeyFilePath = (TextView)findViewById(R.id.publicKeyFilePath);

        publicKeyFilePath.setText(getIntent().getStringExtra("myPublicKeyFilePath"));

        //Initialize multi format writer
        MultiFormatWriter writer = new MultiFormatWriter();
        try {
            //Initialize bit matrix
            BitMatrix matrix = writer.encode(getIntent().getStringExtra("myPublicKey"), BarcodeFormat.QR_CODE, 350, 350);
            BarcodeEncoder encoder = new BarcodeEncoder();
            //Initialize bitmap
            Bitmap bitmap = encoder.createBitmap(matrix);
            //set bitmap on image view
            imageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}
