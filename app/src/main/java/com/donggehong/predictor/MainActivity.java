package com.donggehong.predictor;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private Spinner leagueSpinner;
    private EditText homeTeamInput, awayTeamInput;
    private EditText historyHomeInput, historyDrawInput, historyAwayInput;
    private EditText homeFormInput, awayFormInput;
    private TextView resultText;

    // ==================== V3.12 权重矩阵 ====================
    private static final Map<String, Double> HOME_ADVANTAGE = new HashMap<>();
    private static final Map<String, Double> STRONG_WIN = new HashMap<>();
    private static final Map<String, Double> GOAL_EXPECT = new HashMap<>();
    private static final Map<String, Double> FIRST_HALF_GOAL = new HashMap<>();

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

        GOAL_EXPECT.put("韩职", 2.2);
        GOAL_EXPECT.put("欧战", 2.8);
        GOAL_EXPECT.put("芬超", 2.4);
        GOAL_EXPECT.put("瑞超", 2.6);
        GOAL_EXPECT.put("挪超", 3.0);
        GOAL_EXPECT.put("巴甲", 2.3);

        FIRST_HALF_GOAL.put("韩职", 0.35);
        FIRST_HALF_GOAL.put("欧战", 0.42);
        FIRST_HALF_GOAL.put("芬超", 0.38);
        FIRST_HALF_GOAL.put("瑞超", 0.32);
        FIRST_HALF_GOAL.put("挪超", 0.45);
        FIRST_HALF_GOAL.put("巴甲", 0.30);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        leagueSpinner = findViewById(R.id.leagueSpinner);
        homeTeamInput = findViewById(R.id.homeTeamInput);
        awayTeamInput = findViewById(R.id.awayTeamInput);
        historyHomeInput = findViewById(R.id.historyHomeInput);
        historyDrawInput = findViewById(R.id.historyDrawInput);
        historyAwayInput = findViewById(R.id.historyAwayInput);
        homeFormInput = findViewById(R.id.homeFormInput);
        awayFormInput = findViewById(R.id.awayFormInput);
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

        // 解析历史交锋
        int hW = parseIntOrDefault(historyHomeInput.getText().toString(), 0);
        int hD = parseIntOrDefault(historyDrawInput.getText().toString(), 0);
        int hA = parseIntOrDefault(historyAwayInput.getText().toString(), 0);
        int totalHistory = hW + hD + hA;

        // 解析球队状态
        int homeF = parseIntOrDefault(homeFormInput.getText().toString(), 50);
        int awayF = parseIntOrDefault(awayFormInput.getText().toString(), 50);

        if (home.isEmpty() || away.isEmpty()) {
            resultText.setText("⚠️ 请输入主队和客队名称");
            return;
        }

        // ======================================================
        // V3.12 完整预测逻辑
        // ======================================================

        double homeAdv = HOME_ADVANTAGE.getOrDefault(league, 0.20);
        double strongWin = STRONG_WIN.getOrDefault(league, 0.15);
        double goalAvg = GOAL_EXPECT.getOrDefault(league, 2.5);
        double firstHalfRate = FIRST_HALF_GOAL.getOrDefault(league, 0.35);

        // 1. 球队实力评分（综合：名字长度 + 状态 + 历史）
        double homeStrength = (home.length() % 5 + 3) / 10.0 + (homeF - 50) / 100.0;
        double awayStrength = (away.length() % 5 + 3) / 10.0 + (awayF - 50) / 100.0;

        // 历史交锋修正
        if (totalHistory > 0) {
            double homeHistoryRatio = (double) hW / totalHistory;
            double awayHistoryRatio = (double) hA / totalHistory;
            homeStrength += homeHistoryRatio * 0.15;
            awayStrength += awayHistoryRatio * 0.15;
        }

        // 2. 计算胜平负概率
        double homeScore = homeAdv + homeStrength * 0.15;
        double awayScore = awayStrength * 0.15;

        if (homeStrength > awayStrength + 0.25) {
            homeScore += strongWin;
        } else if (awayStrength > homeStrength + 0.25) {
            awayScore += strongWin;
        }

        double total = homeScore + awayScore + 0.6;
        double homeProb = Math.min(homeScore / total, 0.85);
        double awayProb = Math.min(awayScore / total, 0.85);
        double drawProb = Math.max(0.05, 1.0 - homeProb - awayProb);

        // 归一化
        double sum = homeProb + drawProb + awayProb;
        int homeP = (int) Math.round(homeProb / sum * 100);
        int drawP = (int) Math.round(drawProb / sum * 100);
        int awayP = (int) Math.round(awayProb / sum * 100);
        while (homeP + drawP + awayP != 100) {
            if (homeP + drawP + awayP > 100) {
                if (homeP >= drawP && homeP >= awayP) homeP--;
                else if (drawP >= awayP) drawP--;
                else awayP--;
            } else {
                if (homeP <= drawP && homeP <= awayP) homeP++;
                else if (drawP <= awayP) drawP++;
                else awayP++;
            }
        }

        // 3. 半全场预测
        String halfTime;
        String fullTime;
        double rand = Math.random();
        if (rand < 0.3) {
            halfTime = "平";
        } else if (rand < 0.65) {
            halfTime = homeP > awayP ? "主" : "客";
        } else {
            halfTime = homeP > awayP ? "客" : "主";
        }
        if (homeP > awayP) fullTime = "主";
        else if (awayP > homeP) fullTime = "客";
        else fullTime = "平";
        String halfFull = halfTime + fullTime;

        // 4. 进球数预测
        double totalGoals = goalAvg * (1 + (homeStrength - awayStrength) * 0.2);
        totalGoals = Math.max(0.5, Math.min(5.0, totalGoals));
        int goals;
        if (totalGoals < 1.0) goals = 0;
        else if (totalGoals < 1.8) goals = 1 + (int)(Math.random() * 0.5);
        else if (totalGoals < 2.8) goals = 2 + (int)(Math.random() * 0.8);
        else goals = 3 + (int)(Math.random() * 1.5);

        // 5. 比分预测（简化泊松分布）
        int homeGoals = (int) Math.round(totalGoals * homeProb);
        int awayGoals = (int) Math.round(totalGoals * awayProb);
        if (homeGoals + awayGoals == 0) {
            homeGoals = 1;
            awayGoals = 0;
        }
        String score = homeGoals + " - " + awayGoals;

        // 6. 生成预测结论
        String prediction;
        if (homeP > drawP && homeP > awayP) {
            prediction = "🔴 主胜";
        } else if (awayP > drawP && awayP > homeP) {
            prediction = "🔵 客胜";
        } else {
            prediction = "⚪ 平局";
        }

        // 7. 输出完整结果
        String result = "🏠 " + home + " vs " + away + " ✈️\n";
        result += "📋 联赛：" + league + "\n";
        result += "━━━━━━━━━━━━━━━━━━\n\n";
        result += "📊 胜平负概率\n";
        result += "🟢 主胜 " + homeP + "%\n";
        result += "🟡 平局 " + drawP + "%\n";
        result += "🔵 客胜 " + awayP + "%\n\n";
        result += "⚽ 预测结论：" + prediction + "\n";
        result += "📈 半全场：" + halfFull + "\n";
        result += "🎯 总进球数：" + goals + " 球\n";
        result += "📝 比分预测：" + score + "\n";
        if (totalHistory > 0) {
            result += "📊 历史交锋：" + hW + "胜 " + hD + "平 " + hA + "负\n";
        }
        if (homeF != 50 || awayF != 50) {
            result += "📈 状态评分：主队" + homeF + " | 客队" + awayF + "\n";
        }
        result += "━━━━━━━━━━━━━━━━━━\n";
        result += "📊 V3.12 权重模型";

        resultText.setText(result);
    }

    private int parseIntOrDefault(String s, int def) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }
}
