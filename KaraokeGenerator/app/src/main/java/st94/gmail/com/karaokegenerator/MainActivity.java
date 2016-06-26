package st94.gmail.com.karaokegenerator;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

import st94.gmail.com.karaokegenerator.network.*;

public class MainActivity extends AppCompatActivity {

    private static final String TAG ="MainActivity";
    private static final int ACTIVITY_RESULT_ID = 1;
    Button mUploadButton;

    private final String twoHyphens = "--";
    private final String lineEnd = "\r\n";
    private final String boundary = "apiclient-" + System.currentTimeMillis();
    private final String mimeType = "multipart/form-data;boundary=" + boundary;
    private byte[] multipartBody;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mUploadButton = (Button) findViewById(R.id.report_symptoms_button);
        mUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                String[] mimeTypes = {"audio/mpeg"};
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
                MultipartRequest songUploadRequest = new MultipartRequest(
                    Constants.SERVER_URL + Constants.GLOBAL_ROUTE + Constants.UPLOAD_API_ENDPOINT,
                    null,
                    mimeType,
                    multipartBody,
                    new Response.Listener<NetworkResponse>()
                    {
                        @Override
                        public void onResponse(NetworkResponse response) {
                            Log.i(TAG, "Got a response from the server");

                            if (response.statusCode == 200){
                                //get the processed song back
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Post request to send gcm token to server failed", error);
                        }
                    });
                songUploadRequest.setRetryPolicy(new DefaultRetryPolicy(
                        75000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


                mQueue.add(songUploadRequest);
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
        dataOutputStream.writeBytes("Content-Type: audio/mp3" + lineEnd);
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

}
