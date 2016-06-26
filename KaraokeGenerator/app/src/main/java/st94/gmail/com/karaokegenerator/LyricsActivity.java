package st94.gmail.com.karaokegenerator;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class LyricsActivity extends Activity {

    TextView lyricsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyrics);

        Intent i = getIntent();
        lyricsText = (TextView) findViewById(R.id.lyrics_text);
        lyricsText.setText(i.getStringExtra("SONG_LYRICS"));
    }
}
