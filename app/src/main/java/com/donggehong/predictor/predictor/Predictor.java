package com.donggehong.predictor.predictor;

import com.donggehong.predictor.network.models.TeamData;

import java.util.HashMap;
import java.util.Map;

public class Predictor {
    private static final Map<String, Double> HOME_ADV = new HashMap<>();
    private static final Map<String, Double> STRONG_WIN = new HashMap<>();
    private static final Map<String, Double> GOAL_EXPECT = new HashMap<>();

    static {
        HOME_ADV.put("韩职", 0.07);  HOME_ADV.put("欧战", 0.18);
        HOME_ADV.put("芬超", 0.28); HOME_ADV.put("瑞超", 0.20);
        HOME_ADV.put("挪超", 0.32); HOME_ADV.put("巴甲", 0.36);
        HOME_ADV.put("德甲", 0.25); HOME_ADV.put("德乙", 0.22);
        HOME_ADV.put("default", 0.20);

        STRONG_WIN.put("韩职", 0.10); STRONG_WIN.put("欧战", 0.20);
        STRONG_WIN.put("芬超", 0.10); STRONG_WIN.put("瑞超", 0.20);
        STRONG_WIN.put("挪超", 0.18); STRONG_WIN.put("巴甲", 0.18);
        STRONG_WIN.put("德甲", 0.25); STRONG_WIN.put("德乙", 0.20);
        STRONG_WIN.put("default", 0.15);

        GOAL_EXPECT.put("韩职", 2.2); GOAL_EXPECT.put("欧战", 2.8);
        GOAL_EXPECT.put("芬超", 2.4); GOAL_EXPECT.put("瑞超", 2.6);
        GOAL_EXPECT.put("挪超", 3.0); GOAL_EXPECT.put("巴甲", 2.3);
        GOAL_EXPECT.put("德甲", 3.2); GOAL_EXPECT.put("德乙", 2.8);
        GOAL_EXPECT.put("default", 2.5);
    }

    public PredictionResult predict(TeamData home, TeamData away, String league) {
        double homeAdv = HOME_ADV.getOrDefault(league, HOME_ADV.get("default"));
        double strongWin = STRONG_WIN.getOrDefault(league, STRONG_WIN.get("default"));
        double goalAvg = GOAL_EXPECT.getOrDefault(league, GOAL_EXPECT.get("default"));

        double homeStrength = calculateStrength(home);
        double awayStrength = calculateStrength(away);

        double homeScore = homeAdv + homeStrength * 0.15;
        double awayScore = awayStrength * 0.15;

        if (homeStrength > awayStrength + 0.2) homeScore += strongWin;
        else if (awayStrength > homeStrength + 0.2) awayScore += strongWin;

        double total = homeScore + awayScore + 0.6;
        double homeProb = Math.min(homeScore / total, 0.85);
        double awayProb = Math.min(awayScore / total, 0.85);
        double drawProb = Math.max(0.05, 1.0 - homeProb - awayProb);

        double sum = homeProb + drawProb + awayProb;
        int homeP = (int) Math.round(homeProb / sum * 100);
        int drawP = (int) Math.round(drawProb / sum * 100);
        int awayP = (int) Math.round(awayProb / sum * 100);

        // 修正四舍五入误差
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

        String halfFull = determineHalfFull(homeP, awayP);
        int totalGoals = estimateTotalGoals(goalAvg, homeStrength, awayStrength);
        String score = estimateScore(homeP, awayP, totalGoals);

        return new PredictionResult(homeP, drawP, awayP, halfFull, totalGoals, score);
    }

    private double calculateStrength(TeamData data) {
        if (data == null) return 0.5;
        int total = data.wins + data.draws + data.losses;
        if (total == 0) return 0.5;
        double winRate = (double) data.wins / total;
        double formFactor = formToScore(data.form);
        return winRate * 0.7 + formFactor * 0.3;
    }

    private double formToScore(String form) {
        if (form == null || form.isEmpty()) return 0.5;
        int score = 0;
        for (char c : form.toCharArray()) {
            if (c == '胜' || c == 'W') score += 3;
            else if (c == '平' || c == 'D') score += 1;
        }
        return score / (form.length() * 3.0);
    }

    private String determineHalfFull(int homeP, int awayP) {
        String half = Math.random() > 0.5 ? (homeP > awayP ? "主" : "客") : "平";
        String full = homeP > awayP ? "主" : (awayP > homeP ? "客" : "平");
        return half + full;
    }

    private int estimateTotalGoals(double avg, double homeStr, double awayStr) {
        double factor = 1 + (homeStr - awayStr) * 0.2;
        double expected = avg * factor;
        return (int) Math.max(0, Math.round(expected));
    }

    private String estimateScore(int homeP, int awayP, int total) {
        if (total == 0) return "0-0";
        double homeRatio = homeP / 100.0;
        int homeGoals = (int) Math.round(total * homeRatio);
        int awayGoals = total - homeGoals;
        if (homeGoals < 0) homeGoals = 0;
        if (awayGoals < 0) awayGoals = 0;
        return homeGoals + "-" + awayGoals;
    }
}
