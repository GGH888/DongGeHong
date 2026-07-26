package com.donggehong.predictor.network;

import android.content.Context;
import android.util.Log;

import com.donggehong.predictor.network.models.*;
import com.donggehong.predictor.utils.CacheManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.List;

public class DataFetcher {
    private static final String TAG = "DataFetcher";
    private FootballApi openLigaApi;
    private FootballApi sportScoreApi;
    private CacheManager cache;

    public DataFetcher(Context context) {
        this.cache = new CacheManager(context);

        Retrofit openLigaRetrofit = new Retrofit.Builder()
                .baseUrl(ApiConfig.OPENLIGA_BASE)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        openLigaApi = openLigaRetrofit.create(FootballApi.class);

        Retrofit sportScoreRetrofit = new Retrofit.Builder()
                .baseUrl(ApiConfig.SPORTSCORE_BASE)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        sportScoreApi = sportScoreRetrofit.create(FootballApi.class);
    }

    public void fetchTeamData(String teamName, String league, DataCallback callback) {
        // 先查缓存
        TeamData cached = cache.getTeam(teamName);
        if (cached != null) {
            callback.onSuccess(cached);
            return;
        }

        // 德甲/德乙 → OpenLigaDB
        if (league.contains("德甲") || league.contains("德乙")) {
            fetchFromOpenLiga(teamName, league, callback);
        } else {
            // 其他联赛暂不支持自动获取（可扩展 SportScore）
            callback.onError("当前仅支持德甲/德乙联赛自动获取数据");
        }
    }

    private void fetchFromOpenLiga(String teamName, String league, DataCallback callback) {
        String shortcut = league.contains("德甲") ? "bl1" : "bl2";
        openLigaApi.getOpenLigaMatches(shortcut).enqueue(new Callback<List<OpenLigaMatch>>() {
            @Override
            public void onResponse(Call<List<OpenLigaMatch>> call, Response<List<OpenLigaMatch>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TeamData data = parseOpenLigaData(response.body(), teamName);
                    if (data != null) {
                        cache.saveTeam(teamName, data);
                        callback.onSuccess(data);
                    } else {
                        callback.onError("未在德甲/德乙中找到球队: " + teamName);
                    }
                } else {
                    callback.onError("OpenLigaDB 请求失败");
                }
            }

            @Override
            public void onFailure(Call<List<OpenLigaMatch>> call, Throwable t) {
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }

    private TeamData parseOpenLigaData(List<OpenLigaMatch> matches, String teamName) {
        int wins = 0, draws = 0, losses = 0;
        StringBuilder form = new StringBuilder();
        int totalGoalsFor = 0, totalGoalsAgainst = 0;
        int matchCount = 0;

        for (OpenLigaMatch m : matches) {
            boolean isHome = m.homeTeam.teamName.equalsIgnoreCase(teamName);
            boolean isAway = m.awayTeam.teamName.equalsIgnoreCase(teamName);
            if (!isHome && !isAway) continue;

            // 获取最终比分（取最后一条进球记录）
            int homeScore = 0, awayScore = 0;
            if (m.goals != null && !m.goals.isEmpty()) {
                OpenLigaMatch.Goal last = m.goals.get(m.goals.size() - 1);
                homeScore = last.scoreTeam1;
                awayScore = last.scoreTeam2;
            }

            int teamGoals = isHome ? homeScore : awayScore;
            int opponentGoals = isHome ? awayScore : homeScore;

            totalGoalsFor += teamGoals;
            totalGoalsAgainst += opponentGoals;
            matchCount++;

            if (teamGoals > opponentGoals) {
                wins++;
                form.append("胜");
            } else if (teamGoals == opponentGoals) {
                draws++;
                form.append("平");
            } else {
                losses++;
                form.append("负");
            }
        }

        if (matchCount == 0) return null;

        TeamData data = new TeamData();
        data.name = teamName;
        data.wins = wins;
        data.draws = draws;
        data.losses = losses;
        data.form = form.length() > 0 ? form.toString() : "无数据";
        data.lastFive = data.form.length() > 5 ? data.form.substring(0, 5) : data.form;
        data.avgGoalsFor = (double) totalGoalsFor / matchCount;
        data.avgGoalsAgainst = (double) totalGoalsAgainst / matchCount;
        data.league = "德甲/德乙";
        return data;
    }
                  }
