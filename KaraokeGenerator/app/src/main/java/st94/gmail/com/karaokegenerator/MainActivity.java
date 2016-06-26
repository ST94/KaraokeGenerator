package st94.gmail.com.karaokegenerator;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import st94.gmail.com.karaokegenerator.Models.SongUploadResponse;
import st94.gmail.com.karaokegenerator.network.*;

public class MainActivity extends AppCompatActivity {

    private static final String TAG ="MainActivity";
    private static final int ACTIVITY_RESULT_ID = 1;
    Button mUploadButton;
    CircularProgressBar mCircularLoadingBar;
    TextView mMainTextField;

    private final String twoHyphens = "--";
    private final String lineEnd = "\r\n";
    private final String boundary = "apiclient-" + System.currentTimeMillis();
    private final String mimeType = "multipart/form-data;boundary=" + boundary;
    private byte[] multipartBody;
    private boolean mSuccess = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mCircularLoadingBar = (CircularProgressBar) findViewById(R.id.loading_circle_bar);
        mMainTextField = (TextView) findViewById(R.id.main_text_field);

        mUploadButton = (Button) findViewById(R.id.upload_song_button);
        mUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUploadButton.setEnabled(false);
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                String[] mimeTypes = {"audio/mpeg", "audio/ogg"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                startActivityForResult(intent, ACTIVITY_RESULT_ID);
            }
        });
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String mp3Path = "";
        if (requestCode == ACTIVITY_RESULT_ID) {
            mp3Path = data.getDataString();
            RequestQueue mQueue = Volley.newRequestQueue(getApplicationContext());
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            File mp3File = null;
            byte[] songData = {};

            try {
                String realMP3Path = FilePath.getPath(getApplicationContext(), Uri.parse(mp3Path));
                mp3File = new File (realMP3Path);
                songData = org.apache.commons.io.FileUtils.readFileToByteArray(mp3File);
            }
            catch (Exception e){
                Log.e (TAG, "Failed to create byte array: ", e);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            try {
                if (mp3File != null)
                    buildPart(dos, songData, mp3File.getName());
                // send multipart form data necesssary after file data
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                // pass to multipart body
                multipartBody = bos.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Map<String, String> headers = new ArrayMap<String, String>();
            headers.put("Content-Type", "application/json; charset=utf-8");

            if (networkInfo != null && networkInfo.isConnected())
            {
                // fetch data
                final MultipartRequest songUploadRequest = new MultipartRequest(
                    Constants.SERVER_URL + Constants.GLOBAL_ROUTE + Constants.UPLOAD_API_ENDPOINT,
                    null,
                    mimeType,
                    multipartBody,
                    new Response.Listener<JSONObject>()
                    {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.i(TAG, "Got a response from the server");
                            //Log.i (TAG, "INSIDE MAIN" + response.toString());
                            Gson gson = new Gson();
                            SongUploadResponse responseCode = gson.fromJson (response.toString(), SongUploadResponse.class);

                            if (responseCode.status_code == 200){
                                getKaraokeFile(responseCode.name, responseCode.identifier, responseCode.lyrics);

                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Post request to upload song failed", error);
                        }
                    });
                songUploadRequest.setRetryPolicy(new DefaultRetryPolicy(
                        75000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                mQueue.add(songUploadRequest);
                mMainTextField.setText("Generating your karaoke file!");
                mCircularLoadingBar.setVisibility(View.VISIBLE);
            } else {
                Log.e(TAG, "Failed to connect to server to send gcm token");
                // display error
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void buildPart(DataOutputStream dataOutputStream, byte[] fileData, String fileName) throws IOException {
        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\""
                + fileName + "\"" + lineEnd);
        dataOutputStream.writeBytes("Content-Type: audio/ogg" + lineEnd);
        dataOutputStream.writeBytes(lineEnd);

        ByteArrayInputStream fileInputStream = new ByteArrayInputStream(fileData);
        int bytesAvailable = fileInputStream.available();

        int maxBufferSize = 1024 * 1024;
        int bufferSize = Math.min(bytesAvailable, maxBufferSize);
        byte[] buffer = new byte[bufferSize];

        // read file and write it into form...
        int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

        while (bytesRead > 0) {
            dataOutputStream.write(buffer, 0, bufferSize);
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        }

        dataOutputStream.writeBytes(lineEnd);
    }

    private void getKaraokeFile (final String fileName, final String identifier, final String lyrics)
    {
        SongRetrievalRequest request = new SongRetrievalRequest(
                Constants.SERVER_URL + Constants.MEDIA_API_ENDPOINT + identifier,
                null,
                new Response.Listener<byte[]>() {
                    @Override
                    public void onResponse(byte[] response) {
                        try {
                            if (response != null) {
                                Log.i(TAG,"Writing karaoke file");
                                FileOutputStream outputStream;


                                String name = identifier;
                                File newSongFile = new File(getApplicationContext().getExternalFilesDir(null), identifier);

                                try {
                                    // Very simple code to copy a picture from the application's
                                    // resource into the external file.  Note that this code does
                                    // no error checking, and assumes the picture is small (does not
                                    // try to copy it in chunks).  Note that if external storage is
                                    // not currently mounted this will silently fail.

                                    OutputStream os = new FileOutputStream(newSongFile);
                                    os.write(response);
                                    os.close();
                                } catch (IOException e) {
                                    // Unable to create file, likely because external storage is
                                    // not currently mounted.
                                    Log.w("ExternalStorage", "Error writing " + newSongFile, e);
                                }

                                Log.i (TAG, "Wrote song file to " + identifier);
                                File[] files = getApplicationContext().getExternalFilesDir(null).listFiles();
                                for (File file : files) {
                                    String karaokeFileName =  file.getName();
                                    //Log.i(TAG, karaokeFileName + "    " + identifier);
                                    if (karaokeFileName.equals(identifier)){
                                        mCircularLoadingBar.setVisibility(View.INVISIBLE);
                                        Log.i (TAG, "switching to playing activity");
                                        Intent i = new Intent(getApplicationContext(), PlayActivity.class);
                                        i.putExtra("SONG_FILE_NAME", fileName);
                                        i.putExtra("SONG_ACTUAL_FILE_NAME", identifier);
                                        i.putExtra("SONG_FULL_PATH", file.getAbsolutePath());
                                        i.putExtra("SONG_LYRICS", lyrics);
                                        startActivity (i);
                                    }
                                }

                            }
                            else {
                                Log.i(TAG, "Failed to receive response");
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            Log.d("KEY_ERROR", "UNABLE TO DOWNLOAD FILE");
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // TODO handle the error
                    error.printStackTrace();
                }
        });
        RequestQueue mRequestQueue = Volley.newRequestQueue(getApplicationContext(), new HurlStack());
        mRequestQueue.add(request);
    }

}
