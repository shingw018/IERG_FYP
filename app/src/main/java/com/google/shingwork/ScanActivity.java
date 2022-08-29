package com.google.shingwork;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class ScanActivity extends AppCompatActivity {
    private SurfaceView surfaceView;
    private CameraSource cameraSource;
    private Button gallery;
    private BarcodeDetector barcodeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        surfaceView = findViewById(R.id.camera);

        barcodeDetector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.QR_CODE).build();

        cameraSource = new CameraSource.Builder(getApplicationContext(),barcodeDetector)
                .setRequestedPreviewSize(640,480).setAutoFocusEnabled(true).build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    return;
                }
                try{
                    cameraSource.start(holder);
                } catch (IOException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {}

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {}
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {}

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrcode = detections.getDetectedItems();
                if(qrcode.size() != 0){
                    new Thread()
                    {
                        public  void run()
                        {
                            ScanActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dialogReceiver(qrcode.valueAt(0).rawBytes);
                                    cameraSource.stop();
                                }
                            });
                        }
                    }.start();
                }
            }
        });

        gallery = (Button)findViewById(R.id.gallery);
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 3);
            }
        });
    }

    private void dialogReceiver(byte[] publicKey)
    {
        // show dialog for inputting the password
        final EditText editText = new EditText(ScanActivity.this);
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(ScanActivity.this);
        inputDialog.setTitle("Enter the name of the receiver").setView(editText);
        inputDialog.setPositiveButton("Confirm",
                new DialogInterface.OnClickListener()
                {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Toast.makeText(ScanActivity.this, editText.getText().toString(), Toast.LENGTH_SHORT).show();
                        getIntent().putExtra("receiverPublicKey", publicKey);
                        getIntent().putExtra("receiverName", editText.getText().toString());
                        setResult(RESULT_OK, getIntent());
                        dialog.dismiss();
                        finish();
                    }
                }).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ScanActivity.this, MainActivityFinal.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case 3:
                if(resultCode == RESULT_OK)
                {
                        try{
                            final Uri imageUri = data.getData();
                            final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                            final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                            try{
                                Bitmap bitmap = selectedImage;

                                byte[] contents = null;
                                int[] intArray = new int[bitmap.getWidth()*bitmap.getHeight()];
                                bitmap.getPixels(intArray,0, bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());

                                LuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(),intArray);
                                BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

                                Reader reader = new MultiFormatReader();
                                Result result = reader.decode(binaryBitmap);
                                contents = result.getText().getBytes();
                                dialogReceiver(contents);
                            } catch (NotFoundException e) {
                                e.printStackTrace();
                            } catch (FormatException e) {
                                e.printStackTrace();
                            } catch (ChecksumException e) {
                                e.printStackTrace();
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                }
        }
    }
}

