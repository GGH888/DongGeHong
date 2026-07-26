package com.donggehong.predictor;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.donggehong.predictor.network.DataFetcher;
import com.donggehong.predictor.network.DataCallback;
import com.donggehong.predictor.network.models.TeamData;
import com.donggehong.predictor.predictor.Predictor;
import com.donggehong.predictor.predictor.PredictionResult;

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

        // 联赛选项（包含德甲/德乙）
        String[] leagues = {"韩职", "欧战", "芬超", "瑞超", "挪超", "巴甲", "德甲", "德乙"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, leagues);
        leagueSpinner.setAdapter(adapter);

        // ===== 新增：自动获取数据按钮 =====
        findViewById(R.id.fetchDataBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String home = homeTeamInput.getText().toString().trim();
                String away = awayTeamInput.getText().toString().trim();
                String league = leagueSpinner.getSelectedItem().toString();

                if (home.isEmpty() || away.isEmpty()) {
                    Toast.makeText(MainActivity.this, "请先输入主队和客队名称", Toast.LENGTH_SHORT).show();
                    return;
                }

                resultText.setText("📡 正在获取 " + home + " 的数据...");

                DataFetcher fetcher = new DataFetcher(MainActivity.this);
                fetcher.fetchTeamData(home, league, new DataCallback() {
                    @Override
                    public void onSuccess(TeamData data) {
                        String msg = "🏠 " + home + "\n";
                        msg += "近10场: " + data.wins + "胜 " + data.draws + "平 " + data.losses + "负\n";
                        msg += "状态: " + data.form + "\n";
                        msg += "场均进球: " + String.format("%.2f", data.avgGoalsFor) + "\n";
                        msg += "场均失球: " + String.format("%.2f", data.avgGoalsAgainst) + "\n";

                        runOnUiThread(() -> {
                            resultText.setText(msg + "\n✅ 主队数据获取成功！\n点击「开始预测」查看完整结果");
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            resultText.setText("❌ " + error + "\n提示：德甲/德乙球队数据自动获取，其他联赛请手动输入");
                        });
                    }
                });
            }
        });

        // ===== 原“开始预测”按钮（增强版，使用 V3.12 权重模型） =====
        findViewById(R.id.predictBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String home = homeTeamInput.getText().toString().trim();
                String away = awayTeamInput.getText().toString().trim();
                String league = leagueSpinner.getSelectedItem().toString();

                if (home.isEmpty() || away.isEmpty()) {
                    Toast.makeText(MainActivity.this, "请先输入球队名称", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 构造球队数据（实际应使用从网络获取的缓存数据，这里用模拟数据演示）
                TeamData homeData = new TeamData();
                homeData.name = home;
                homeData.wins = 5; homeData.draws = 3; homeData.losses = 2;
                homeData.form = "胜胜平平胜";
                homeData.avgGoalsFor = 1.8;
                homeData.avgGoalsAgainst = 1.2;

                TeamData awayData = new TeamData();
                awayData.name = away;
                awayData.wins = 4; awayData.draws = 2; awayData.losses = 4;
                awayData.form = "胜负平胜负";
                awayData.avgGoalsFor = 1.5;
                awayData.avgGoalsAgainst = 1.6;

                Predictor predictor = new Predictor();
                PredictionResult result = predictor.predict(homeData, awayData, league);

                String output = "🏠 " + home + " vs " + away + " ✈️\n";
                output += "📋 联赛：" + league + "\n\n";
                output += "🟢 主胜 " + result.homeProb + "%\n";
                output += "🟡 平局 " + result.drawProb + "%\n";
                output += "🔵 客胜 " + result.awayProb + "%\n\n";
                output += "⚽ 预测结论：" + (result.homeProb > result.drawProb && result.homeProb > result.awayProb ? "主胜" :
                                             (result.awayProb > result.drawProb ? "客胜" : "平局")) + "\n";
                output += "📈 半全场：" + result.halfFull + "\n";
                output += "🎯 总进球数：" + result.totalGoals + " 球\n";
                output += "📝 比分预测：" + result.score + "\n\n";
                output += "📊 V3.12 权重模型";

                resultText.setText(output);
            }
        });
    }
}        // V3.12 完整预测逻辑
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
