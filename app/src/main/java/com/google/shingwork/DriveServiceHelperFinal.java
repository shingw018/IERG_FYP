package com.google.shingwork;

import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DriveServiceHelperFinal {
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private Drive mDriveService;

    public DriveServiceHelperFinal(Drive mDriveService)
    {
        this.mDriveService = mDriveService;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Task<String> uploadFile(java.io.File file)
    {
        return Tasks.call(mExecutor, ()-> {
            File fileMetaData = new File();

            fileMetaData.setName(file.getName().split("\\.")[0]);

            FileContent mediaContent = new FileContent(Files.probeContentType(file.toPath()), file);

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

    public Task<String> downloadFile(String fileId, String filename)
    {
        return Tasks.call(mExecutor, ()-> {
            //java.io.File file = new java.io.File(Environment.getExternalStorageDirectory(), filename);
            java.io.File root = new java.io.File(Environment.getExternalStorageDirectory(), MainActivityFinal.folderRoot + "/" + MainActivityFinal.folderDownloaded);
            if (!root.exists()) {
                root.mkdirs();
            }
            java.io.File file = new java.io.File(root, filename);

            OutputStream outputStream = new FileOutputStream(file.getAbsolutePath());
            mDriveService.files().get(fileId)
                    .executeMediaAndDownloadTo(outputStream);

            return file.getAbsolutePath();

        });
    }

    public Task<HashMap<String, String>> retrieve()
    {
        return Tasks.call(mExecutor, ()-> {
            HashMap<String, String> datatype = new HashMap<String, String>();
            datatype.put("mimeType='text/plain'", ".txt");
            datatype.put("mimeType='text/html'", ".html");
            datatype.put("mimeType='text/html'", ".htm");
            datatype.put("mimeType='text/css'", ".css");
            datatype.put("mimeType='text/javascript'", ".js");
            datatype.put("mimeType='text/csv'", ".csv");
            datatype.put("mimeType='image/gif'", ".gif");
            datatype.put("mimeType='image/png'", ".png");
            datatype.put("mimeType='image/jpeg'", ".jpeg");
            datatype.put("mimeType='image/jpeg'", ".jpg");
            datatype.put("mimeType='image/bmp	'", ".bmp");
            datatype.put("mimeType='image/webp'", ".webp");
            datatype.put("mimeType='video/mp4'", ".mp4");
            datatype.put("mimeType='audio/mpeg'", ".mp3");
            datatype.put("mimeType='application/pdf'", ".pdf");
            datatype.put("mimeType='application/msword'", ".doc");
            datatype.put("mimeType='application/json'", ".json");
            datatype.put("mimeType='application/x-httpd-php'", ".php");
            datatype.put("mimeType='application/vnd.ms-powerpoint'", ".ppt");
            datatype.put("mimeType='application/vnd.openxmlformats-officedocument.presentationml.presentation'", ".pptx");
            datatype.put("mimeType='application/vnd.openxmlformats-officedocument.wordprocessingml.document'", ".docx");

            HashMap<String, String> filedata = new HashMap<String, String>();
            String pageToken = null;
            do{
                for(String type : datatype.keySet()) {
                    FileList result = mDriveService.files().list()
                            .setQ(type)
                            //.setQ("mimeType='image/jpeg'")
                            .setSpaces("drive")
                            .setFields("nextPageToken, files(id, name)")
                            .setPageToken(pageToken)
                            .execute();
                    for (File file : result.getFiles()) {
                        filedata.put(file.getName()+datatype.get(type).toString(), file.getId());
                    }
                    pageToken = result.getNextPageToken();
                }
            } while (pageToken != null);

            return filedata;
        });
    }
}
