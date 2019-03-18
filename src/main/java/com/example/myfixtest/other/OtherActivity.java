package com.example.myfixtest.other;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.example.myfixtest.R;

public class OtherActivity extends AppCompatActivity {


    private TextView tv_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other);
        tv_text = findViewById(R.id.tv_text);

    }

    public void change(View view) {
        tv_text.setText(" this is other activity ,i'm good.");
    }
}
