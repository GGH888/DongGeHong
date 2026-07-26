package com.donggehong.predictor.predictor;

public class PredictionResult {
    public int homeProb;
    public int drawProb;
    public int awayProb;
    public String halfFull;
    public int totalGoals;
    public String score;

    public PredictionResult(int home, int draw, int away, String halfFull, int goals, String score) {
        this.homeProb = home;
        this.drawProb = draw;
        this.awayProb = away;
        this.halfFull = halfFull;
        this.totalGoals = goals;
        this.score = score;
    }
}
