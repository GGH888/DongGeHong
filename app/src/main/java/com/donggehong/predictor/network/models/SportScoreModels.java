package com.donggehong.predictor.network.models;

import java.util.List;

public class SportScoreMatches {
    public List<Match> data;
    public static class Match {
        public int id;
        public String homeTeam;
        public String awayTeam;
        public String homeScore;
        public String awayScore;
        public String status;
        public String date;
    }
}

public class SportScoreStandings {
    public List<Standing> data;
    public static class Standing {
        public int position;
        public String team;
        public int points;
        public int played;
        public int win;
        public int draw;
        public int lose;
    }
}
