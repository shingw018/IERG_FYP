package com.google.shingwork;

import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DriveServiceHelper {

    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private Drive mDriveService;

    public DriveServiceHelper(Drive mDriveService)
    {
        this.mDriveService = mDriveService;
    }

    public Task<String> createFilePDF(String filePath)
    {
        return Tasks.call(mExecutor, ()-> {
            File fileMetaData = new File();
            fileMetaData.setName("ENCRYPTED");

            java.io.File file = new java.io.File(filePath);

            FileContent mediaContent = new FileContent("image/jpeg", file);

            File myFile = null;
            try
            {
                myFile = mDriveService.files().create(fileMetaData, mediaContent).execute();
            }catch (Exception e){
                e.printStackTrace();
            }

            if(myFile == null)
            {
                throw new IOException("Null result when requesting file creation");
            }

            return myFile.getId();

        });
    }

    public Task<String> downFilePDF(String fileId)
    {
        return Tasks.call(mExecutor, ()-> {
            String filePath = "/storage/emulated/0/DOWNLOADED.jpg";
            OutputStream outputStream = new FileOutputStream(filePath);
            mDriveService.files().get(fileId)
                    .executeMediaAndDownloadTo(outputStream);
            return null;

        });
    }
}
