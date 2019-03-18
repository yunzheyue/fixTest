package com.example.myfixtest.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.myfixtest.R;
import com.example.myfixtest.other.OtherActivity;
import com.example.myfixtest.utils.FixDexUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void goNext(View view) {
        startActivity(new Intent(this, OtherActivity.class));
    }

    public void fixOther(View view) {
        FixDexUtils.copyFileToPackageAndInstall(this);

    }
}
