package com.google.shingwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity2 extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    // create array of Strings
    // and store name of courses
    ArrayList<String> ar = new ArrayList<String>();
    HashMap<String,String> ghashMap = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Intent intent = getIntent();
        HashMap<String, String> hashMap = (HashMap<String, String>)intent.getSerializableExtra("driveFileList");
        ghashMap = hashMap;
        ar.add("To Select");
        for (String i : hashMap.keySet()) {
            ar.add(i);
        }

        // Take the instance of Spinner and
        // apply OnItemSelectedListener on it which
        // tells which item of spinner is clicked
        Spinner spino = findViewById(R.id.planets_spinner);
        spino.setOnItemSelectedListener(this);

        // Create the instance of ArrayAdapter
        // having the list of courses
        ArrayAdapter ad = new ArrayAdapter(this, android.R.layout.simple_spinner_item, ar);

        // set simple layout resource file
        // for each item of spinner
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set the ArrayAdapter (ad) data on the
        // Spinner which binds data to spinner
        spino.setAdapter(ad);
    }

    // Performing action when ItemSelected
    // from spinner, Overriding onItemSelected method
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // which is selected in spinner
        Toast.makeText(getApplicationContext(), ar.get(position),Toast.LENGTH_LONG).show();
        if(!(ar.get(position).equals("To Select"))) {
            String fileid = ghashMap.get(ar.get(position));
            String filename = ar.get(position);
            getIntent().putExtra("downloadFileName", filename);
            getIntent().putExtra("downloadFileId", fileid);
            setResult(RESULT_OK, getIntent());
            finish();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent /*AdapterView<*> arg0*/) {
        // Auto-generated method stub
    }
}

