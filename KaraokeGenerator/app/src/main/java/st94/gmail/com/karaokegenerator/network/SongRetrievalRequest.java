package st94.gmail.com.karaokegenerator.network;

/**
 * Created by Shing on 2016-06-25.
 */
import android.nfc.Tag;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class SongRetrievalRequest extends Request<byte[]> {
    private final Response.Listener<byte[]> mListener;
    private final Response.ErrorListener mErrorListener;
    private final Map<String, String> mHeaders;
    //private final String mMimeType;
    //private final byte[] mMultipartBody;
    private final String TAG = "MultiPartRequest";
    public int mStatusCode = 0;

    public SongRetrievalRequest(String url, Map<String, String> headers, Response.Listener<byte[]> listener, Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        this.mListener = listener;
        this.mErrorListener = errorListener;
        this.mHeaders = headers;
        //this.mMimeType = mimeType;
        //this.mMultipartBody = multipartBody;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return (mHeaders != null) ? mHeaders : super.getHeaders();
    }

    // Same as JsonObjectRequest#parseNetworkResponse
    @Override
    protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {

        //Initialise local responseHeaders map with response headers received
        //responseHeaders = response.headers;

        //Pass the response data here
        Log.i (TAG, "Logging returned data" + response.data);
        return Response.success( response.data, HttpHeaderParser.parseCacheHeaders(response));
    }

//    @Override
//    public String getBodyContentType() {
//        return mMimeType;
//    }
//
//    @Override
//    public byte[] getBody() throws AuthFailureError {
//        return mMultipartBody;
//    }


    @Override
    protected void deliverResponse(byte[] response) {
        mListener.onResponse(response);
    }

    @Override
    public void deliverError(VolleyError error) {
        mErrorListener.onErrorResponse(error);
    }
}
