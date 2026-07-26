package com.donggehong.predictor.network;

import com.donggehong.predictor.network.models.*;
import retrofit2.Call;
import retrofit2.http.*;
import java.util.List;

public interface FootballApi {

    // ===== OpenLigaDB =====
    @GET("getmatchdata/{leagueShortcut}")
    Call<List<OpenLigaMatch>> getOpenLigaMatches(
        @Path("leagueShortcut") String leagueShortcut
    );

    @GET("getbltable/{leagueShortcut}")
    Call<List<OpenLigaTable>> getOpenLigaTable(
        @Path("leagueShortcut") String leagueShortcut
    );

    // ===== SportScore（备用） =====
    @GET("team/{teamId}/matches")
    Call<SportScoreMatches> getTeamMatches(@Path("teamId") int teamId);

    @GET("league/{leagueId}/standings")
    Call<SportScoreStandings> getStandings(@Path("leagueId") int leagueId);
}
