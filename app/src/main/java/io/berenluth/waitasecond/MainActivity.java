package io.berenluth.waitasecond;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    FrameLayout startLayout;
    FrameLayout gameLayout;
    FrameLayout endLayout;

    TextView timeError;
    TextView timeRemains;
    TextView endRes;

    TextView recordText, recordValue;

    int tentativi = 10;
    int perfect = 0;

    Date lastClick;

    ArrayList<Long> res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startLayout = (FrameLayout) findViewById(R.id.start_layout);
        gameLayout = (FrameLayout) findViewById(R.id.game_layout);
        endLayout = (FrameLayout) findViewById(R.id.end_layout);

        timeError = (TextView) findViewById(R.id.time_error_layout);
        timeRemains = (TextView) findViewById(R.id.time_remains);
        endRes = (TextView) findViewById(R.id.end_res);
        res = new ArrayList<>();

        recordText = (TextView) findViewById(R.id.record_text);
        recordValue = (TextView) findViewById(R.id.record_value);

        findViewById(R.id.start_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLayout.setVisibility(View.GONE);
                gameLayout.setVisibility(View.VISIBLE);
                //gameLayout.animate().alpha(1);
                lastClick = new Date();
                reset();
            }
        });

        gameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(tentativi > 0) {
                    tentativi--;
                    long err = (new Date()).getTime() - lastClick.getTime() - 1000;
                    res.add(err);
                    timeError.setText(err + "ms");

                    if(tentativi == 0)
                        timeRemains.setText("END");
                    else
                        timeRemains.setText(String.valueOf(tentativi));

                    if(err == 0)
                        perfect++;

                    lastClick = new Date();

                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if(err != 0)
                        v.vibrate(30);
                    else
                        v.vibrate(200);
                } else {
                    gameLayout.setVisibility(View.GONE);
                    endLayout.setVisibility(View.VISIBLE);
                    long avg = getAvg();
                    endRes.setText(avg + "ms");
                    if( handleRecord(avg) ){
                        recordText.setText("NUOVO RECORD!");
                        recordValue.setText(String.valueOf(avg));
                    } else {
                        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                        long highScore = sharedPref.getLong("record", -1);
                        recordText.setText("Record:");
                        recordValue.setText(String.valueOf(highScore));
                    }
                }

            }
        });

        endLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endLayout.setVisibility(View.GONE);
                startLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    private long getAvg(){
        Log.d("AVG", "numero risultati: " + res.size());
        Log.d("AVG", "numero di perfect: " + perfect);
        long avg = 0;
        for(Long x: res){
            avg += Math.abs(x);
        }
        return avg/res.size();
    }

    private void reset(){
        lastClick = new Date();
        tentativi = 10;
        perfect = 0;
        timeRemains.setText(String.valueOf(tentativi));
        timeError.setText("");
        res.clear();
    }

    private boolean handleRecord(long avg){
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        long highScore = sharedPref.getLong("record", -1);

        if(highScore == -1){
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putLong("record", avg);
            editor.apply();
            return true;
        } else if( highScore > avg ){
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putLong("record", avg);
            editor.apply();
            return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        reset();
        endLayout.setVisibility(View.GONE);
        startLayout.setVisibility(View.VISIBLE);
        gameLayout.setVisibility(View.GONE);
    }
}
