package com.donggehong.predictor.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.donggehong.predictor.network.models.TeamData;
import com.google.gson.Gson;

public class CacheManager {
    private SharedPreferences prefs;
    private Gson gson = new Gson();
    private static final long CACHE_EXPIRY = 30 * 60 * 1000; // 30分钟

    public CacheManager(Context context) {
        prefs = context.getSharedPreferences("cache", Context.MODE_PRIVATE);
    }

    public TeamData getTeam(String name) {
        String json = prefs.getString(name, null);
        if (json == null) return null;
        TeamData data = gson.fromJson(json, TeamData.class);
        long timestamp = prefs.getLong(name + "_time", 0);
        if (System.currentTimeMillis() - timestamp > CACHE_EXPIRY) {
            return null;
        }
        return data;
    }

    public void saveTeam(String name, TeamData data) {
        String json = gson.toJson(data);
        prefs.edit()
                .putString(name, json)
                .putLong(name + "_time", System.currentTimeMillis())
                .apply();
    }

    public void clearCache() {
        prefs.edit().clear().apply();
    }
}
