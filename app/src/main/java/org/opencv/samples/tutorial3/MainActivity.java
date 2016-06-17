package org.opencv.samples.tutorial3;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.BinderThread;
import android.view.View;
import android.widget.EditText;

import org.opencv.samples.tutorial3.R;
import org.opencv.samples.tutorial3.Tutorial3Activity;

public class MainActivity extends Activity {

    private static final int REQUEST_RECOGNIZE = 0;

    EditText edtMaxBP, edtMinBP, edtPulse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initComponents();
    }

    private void initComponents() {
        edtMaxBP = (EditText) findViewById(R.id.edtMaxBP);
        edtMinBP = (EditText) findViewById(R.id.edtMinBP);
        edtPulse = (EditText) findViewById(R.id.edtPulse);
    }

    public void onClick_btnRecognize(View view) {
        startActivityForResult(new Intent(this, Tutorial3Activity.class), REQUEST_RECOGNIZE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_RECOGNIZE) {
                edtMaxBP.setText(data.getStringExtra("maxbp"));
                edtMinBP.setText(data.getStringExtra("minbp"));
                edtPulse.setText(data.getStringExtra("pulse"));
            }
        }
    }
}
