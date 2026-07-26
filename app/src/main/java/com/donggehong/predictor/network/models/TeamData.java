package com.donggehong.predictor.network.models;

public class TeamData {
    public int id;
    public String name;
    public String logo;
    public String league;
    public int rank;
    public int points;

    public int wins;
    public int draws;
    public int losses;
    public String form;       // 如 "胜胜平平负"
    public String lastFive;   // 近5场

    public double avgGoalsFor;
    public double avgGoalsAgainst;
}
