package st94.gmail.com.karaokegenerator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import jp.co.recruit_lifestyle.android.widget.PlayPauseButton;

public class PlayActivity extends AppCompatActivity {

    private MediaPlayer mPlayer;
    private PlayPauseButton mPlayPauseButton;
    private TextView mSongName;
    private Button mLyricsButton;
    private Paint mPaint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        final Intent originalIntent = getIntent();
        mPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(originalIntent.getStringExtra("SONG_FULL_PATH")));

        mSongName = (TextView) findViewById(R.id.song_name_text);
        mSongName.setText(originalIntent.getStringExtra("SONG_FILE_NAME"));

        mPlayPauseButton = (PlayPauseButton) findViewById(R.id.main_play_pause_button);
        mPlayPauseButton.setColor(ContextCompat.getColor(getApplicationContext(),R.color.colorPrimary));
        mPlayPauseButton.setOnControlStatusChangeListener(new PlayPauseButton.OnControlStatusChangeListener() {
            @Override public void onStatusChange(View view, boolean state) {
                if(state) {
                    mPlayer.start();
                } else {
                    mPlayer.pause();
                }
            }
        });

        mLyricsButton = (Button) findViewById(R.id.upload_song_button);
        mLyricsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                AlertDialog.Builder adb = new AlertDialog.Builder(PlayActivity.this)
//                        .setTitle("Song lyrics")
//                        .setMessage(originalIntent.getStringExtra("SONG_LYRICS"));
//                Dialog d = adb.setView(new View(PlayActivity.this)).create();
//
//                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//                lp.copyFrom(d.getWindow().getAttributes());
//                lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
//                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
//                d.show();
//                d.getWindow().setAttributes(lp);
                Intent i = new Intent(getApplicationContext(), LyricsActivity.class);
                i.putExtra("SONG_LYRICS", originalIntent.getStringExtra("SONG_LYRICS"));
                startActivity(i);
            }
        });
    }
}
