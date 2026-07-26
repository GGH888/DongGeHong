package com.donggehong.predictor;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private Spinner leagueSpinner;
    private EditText homeTeamInput, awayTeamInput;
    private TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        leagueSpinner = findViewById(R.id.leagueSpinner);
        homeTeamInput = findViewById(R.id.homeTeamInput);
        awayTeamInput = findViewById(R.id.awayTeamInput);
        resultText = findViewById(R.id.resultText);

        // 联赛选项
        String[] leagues = {"韩职", "欧战", "芬超", "瑞超", "挪超", "巴甲"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, leagues);
        leagueSpinner.setAdapter(adapter);

        // 预测按钮
        findViewById(R.id.predictBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                predict();
            }
        });
    }

    private void predict() {
        String home = homeTeamInput.getText().toString().trim();
        String away = awayTeamInput.getText().toString().trim();

        if (home.isEmpty() || away.isEmpty()) {
            resultText.setText("⚠️ 请输入主队和客队名称");
            return;
        }

        // 模拟预测逻辑（后续可接入V3.12权重模型）
        double homeProb = 35 + new Random().nextInt(30);
        double drawProb = 15 + new Random().nextInt(20);
        double awayProb = 100 - homeProb - drawProb;

        String result = "🏠 " + home + " vs " + away + " ✈️\n";
        result += "主胜 " + String.format("%.0f", homeProb) + "%\n";
        result += "平局 " + String.format("%.0f", drawProb) + "%\n";
        result += "客胜 " + String.format("%.0f", awayProb) + "%\n\n";
        result += "⚽ 预测结果：V3.12 权重模型推演中";

        resultText.setText(result);
    }
}
