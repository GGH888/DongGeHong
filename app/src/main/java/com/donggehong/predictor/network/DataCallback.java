package com.donggehong.predictor.network;

import com.donggehong.predictor.network.models.TeamData;

public interface DataCallback {
    void onSuccess(TeamData data);
    void onError(String error);
}
