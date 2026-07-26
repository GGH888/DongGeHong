package com.donggehong.predictor.network.models;

import java.util.List;

public class OpenLigaMatch {
    public int matchID;
    public String matchDateTime;
    public String timeZoneID;
    public String leagueShortcut;
    public String leagueName;
    public Team homeTeam;
    public Team awayTeam;
    public List<Goal> goals;
    public String matchIsFinished;

    public static class Team {
        public int teamId;
        public String teamName;
        public String shortName;
    }

    public static class Goal {
        public int goalID;
        public int scoreTeam1;
        public int scoreTeam2;
        public int matchMinute;
        public String goalGetterName;
    }
}

public class OpenLigaTable {
    public int leagueID;
    public String leagueName;
    public List<TableEntry> table;

    public static class TableEntry {
        public int rank;
        public String teamName;
        public int points;
        public int matches;
        public int wins;
        public int draws;
        public int losses;
        public int goals;
        public int opponentGoals;
    }
}
