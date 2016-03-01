package com.example.dominik.musicsync;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private final int REQUEST_CODE_SOURCE = 1;
    private final int REQUEST_CODE_TARGET = 2;
    private final int REQUEST_WRITE_PERMISSION = 3;

    private Uri targetURI;
    private Uri sourceURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        boolean hasPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_WRITE_PERMISSION);
        }

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onButton1Clicked(View view)
    {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, REQUEST_CODE_SOURCE);
    }

    public void onButton2Clicked(View view)
    {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, REQUEST_CODE_TARGET);
    }

    public void onButtonStartClicked(View view) throws IOException
    {
        if (targetURI == null || sourceURI == null)
        {
            throw new NullPointerException("source of target is null");
        }

        DocumentFile f = DocumentFile.fromTreeUri(this, sourceURI);
        DocumentFile files[] = f.listFiles();

        if (files == null)
        {
            Toast.makeText(this, "No files in source directory", Toast.LENGTH_LONG).show();
        }
        else
        {
            for (int i=0; i < files.length; i++)
            {
                DocumentFile targetDir = DocumentFile.fromTreeUri(this, targetURI);
                DocumentFile file = files[i];

                try
                {
                    // Create a new file and write into it
                    DocumentFile newFile = targetDir.createFile("audio/mp3", file.getName());
                    InputStream in = getContentResolver().openInputStream(file.getUri());
                    OutputStream out = getContentResolver().openOutputStream(newFile.getUri());

                    // Transfer bytes from in to out
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }

                    in.close();
                    out.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                Toast.makeText(getApplicationContext(), "one file copyied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_WRITE_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    //reload my activity with permission granted or use the features what required the permission
                    // ...
                } else
                {
                    Toast.makeText(this, "The app was not allowed to write to your storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
                }
            }
        }

    })

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        String uriString;

        if (!(resultCode == RESULT_OK))
            return;

        else if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_SOURCE)
            {
                uriString = data.getData().toString();

                if (uriString.startsWith("file://"))
                    sourceURI = Uri.parse(uriString.replace("file://", ""));
                else
                    sourceURI = Uri.parse(uriString);

                TextView txtSource = (TextView)findViewById(R.id.txtSource);
                txtSource.setText("source location: " + sourceURI.toString());
            }
            else if (requestCode == REQUEST_CODE_TARGET)
            {
                uriString = data.getData().toString();

                if (uriString.startsWith("file://"))
                    targetURI = Uri.parse(uriString.replace("file://", ""));
                else
                    targetURI = Uri.parse(uriString);

                TextView txtTarget = (TextView)findViewById(R.id.txtTarget);
                txtTarget.setText("target location: " + targetURI.toString());
            }
        }
    }
}
