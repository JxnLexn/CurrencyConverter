package com.example.currencyconverter;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class ExchangeRateUpdateWorker extends Worker {

    private final OkHttpClient client = new OkHttpClient();

    public ExchangeRateUpdateWorker(@NonNull Context context, @NonNull WorkerParameters updaterParams) {
        super(context, updaterParams);
    }

    @Override
    public Result doWork() {
        try {
            return updateCurrencies() ? Result.success() : Result.failure();
        } catch (Exception e) {
            Log.e("ExchangeRateUpdateWorker", "Error updating currencies", e);
            return Result.failure();
        }
    }

    private boolean updateCurrencies() throws IOException, JSONException {
        String url = "https://www.floatrates.com/daily/eur.json";

        Request request = new Request.Builder().url(url).build();
        ExchangeRateDatabase database = ExchangeRateDatabase.getInstance();
        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            Log.e("UpdateWorker", "Error fetching currency rates");
            return false;
        }

        String responseBody = response.body().string();
        JSONObject root = new JSONObject(responseBody);

        Iterator<String> keys = root.keys();
        while (keys.hasNext()) {
            String currencyCode = keys.next();
            JSONObject currencyObject = root.getJSONObject(currencyCode);
            double rate = currencyObject.getDouble("rate");
            database.setExchangeRate(currencyCode.toUpperCase(), rate);
        }

        Log.d("UpdateWorker", "Currency rates updated successfully");
        return true;
    }
}
