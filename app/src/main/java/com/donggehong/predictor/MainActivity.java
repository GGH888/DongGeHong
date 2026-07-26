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

    // ==================== V3.12 权重矩阵 ====================
    private static final Map<String, Double> HOME_ADVANTAGE = new HashMap<>();
    private static final Map<String, Double> STRONG_WIN = new HashMap<>();
    static {
        HOME_ADVANTAGE.put("韩职", 0.07);
        HOME_ADVANTAGE.put("欧战", 0.18);
        HOME_ADVANTAGE.put("芬超", 0.28);
        HOME_ADVANTAGE.put("瑞超", 0.20);
        HOME_ADVANTAGE.put("挪超", 0.32);
        HOME_ADVANTAGE.put("巴甲", 0.36);

        STRONG_WIN.put("韩职", 0.10);
        STRONG_WIN.put("欧战", 0.20);
        STRONG_WIN.put("芬超", 0.10);
        STRONG_WIN.put("瑞超", 0.20);
        STRONG_WIN.put("挪超", 0.18);
        STRONG_WIN.put("巴甲", 0.18);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        leagueSpinner = findViewById(R.id.leagueSpinner);
        homeTeamInput = findViewById(R.id.homeTeamInput);
        awayTeamInput = findViewById(R.id.awayTeamInput);
        resultText = findViewById(R.id.resultText);

        String[] leagues = {"韩职", "欧战", "芬超", "瑞超", "挪超", "巴甲"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, leagues);
        leagueSpinner.setAdapter(adapter);

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
        String league = leagueSpinner.getSelectedItem().toString();

        if (home.isEmpty() || away.isEmpty()) {
            resultText.setText("⚠️ 请输入主队和客队名称");
            return;
        }

        // ======================================================
        // V3.12 核心权重计算
        // ======================================================

        // 1. 获取联赛权重
        double homeAdv = HOME_ADVANTAGE.getOrDefault(league, 0.20);
        double strongWin = STRONG_WIN.getOrDefault(league, 0.15);

        // 2. 模拟球队实力（基于名字长度做简单模拟，实际可接入真实数据）
        double homeStrength = (home.length() % 5 + 3) / 10.0;
        double awayStrength = (away.length() % 5 + 3) / 10.0;

        // 3. 计算主队得分
        double homeScore = homeAdv + homeStrength * 0.1;
        double awayScore = awayStrength * 0.1;

        // 4. 强队碾压修正
        if (homeStrength > awayStrength + 0.2) {
            homeScore += strongWin;
        } else if (awayStrength > homeStrength + 0.2) {
            awayScore += strongWin;
        }

        // 5. 计算概率
        double total = homeScore + awayScore + 0.5;
        double homeProb = Math.min(homeScore / total, 0.85);
        double awayProb = Math.min(awayScore / total, 0.85);
        double drawProb = Math.max(0, 1.0 - homeProb - awayProb);

        // 6. 确保概率和为100%
        double sum = homeProb + drawProb + awayProb;
        homeProb = Math.round(homeProb / sum * 100);
        drawProb = Math.round(drawProb / sum * 100);
        awayProb = Math.round(awayProb / sum * 100);

        // 7. 修正四舍五入误差
        while (homeProb + drawProb + awayProb != 100) {
            if (homeProb + drawProb + awayProb > 100) {
                if (homeProb >= drawProb && homeProb >= awayProb) homeProb--;
                else if (drawProb >= awayProb) drawProb--;
                else awayProb--;
            } else {
                if (homeProb <= drawProb && homeProb <= awayProb) homeProb++;
                else if (drawProb <= awayProb) drawProb++;
                else awayProb++;
            }
        }

        // 8. 生成预测结论
        String prediction;
        if (homeProb > drawProb && homeProb > awayProb) {
            prediction = "🔴 主胜";
        } else if (awayProb > drawProb && awayProb > homeProb) {
            prediction = "🔵 客胜";
        } else {
            prediction = "⚪ 平局";
        }

        // 9. 显示结果
        String result = "🏠 " + home + " vs " + away + " ✈️\n";
        result += "联赛：" + league + "\n\n";
        result += "🟢 主胜 " + homeProb + "%\n";
        result += "🟡 平局 " + drawProb + "%\n";
        result += "🔵 客胜 " + awayProb + "%\n\n";
        result += "⚽ 预测结果：" + prediction + "\n";
        result += "📊 V3.12 权重模型";

        resultText.setText(result);
    }
}
