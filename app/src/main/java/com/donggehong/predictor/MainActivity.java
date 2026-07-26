package com.donggehong.predictor;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("东哥红 V3.12\n构建成功！");
        tv.setTextSize(28);
        tv.setGravity(android.view.Gravity.CENTER);
        setContentView(tv);
    }
}
