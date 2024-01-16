package com.example.currencyconverter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {
    ExchangeRateDatabase database = new ExchangeRateDatabase();
    Spinner sourceCurrencySpinner;
    Spinner targetCurrencySpinner;
    EditText amount;
    Button calculate;
    private ShareActionProvider shareActionProvider;
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sourceCurrencySpinner = findViewById(R.id.sourceSpinner);
        targetCurrencySpinner = findViewById(R.id.targetSpinner);

        amount = findViewById(R.id.inputAmount);
        calculate = findViewById(R.id.calcButton);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.blue));
        setSupportActionBar(toolbar);

        setupUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);

        MenuItem shareItem = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider)MenuItemCompat.getActionProvider(shareItem);
        setShareText(null);
        return true;
    }

    private void setShareText(String text) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        if (text != null) {
            shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        }
        shareActionProvider.setShareIntent(shareIntent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.my_menu_entry) {
            Intent intent = new Intent(this, CurrencyListActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.refresh_rate) {
            updateCurrencies();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void updateCurrencies() {
        String url = "https://www.floatrates.com/daily/eur.json";

        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Failed to fetch currency rates", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error fetching currency rates", Toast.LENGTH_SHORT).show());
                    return;
                }

                try {
                    String responseBody = response.body().string();
                    JSONObject root = new JSONObject(responseBody);

                    Iterator<String> keys = root.keys();
                    while (keys.hasNext()) {
                        String currencyCode = keys.next();
                        JSONObject currencyObject = root.getJSONObject(currencyCode);
                        double rate = currencyObject.getDouble("rate");

                        database.setExchangeRate(currencyCode.toUpperCase(), rate);
                    }

                    runOnUiThread(() -> {
                        CurrencyItemAdapter sourceAdapter = (CurrencyItemAdapter) sourceCurrencySpinner.getAdapter();
                        CurrencyItemAdapter targetAdapter = (CurrencyItemAdapter) targetCurrencySpinner.getAdapter();
                        if (sourceAdapter != null) {
                            sourceAdapter.notifyDataSetChanged();
                        }
                        if (targetAdapter != null) {
                            targetAdapter.notifyDataSetChanged();
                        }
                        Toast.makeText(MainActivity.this, "Currency rates updated", Toast.LENGTH_SHORT).show();
                    });
                } catch (JSONException e) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error parsing JSON data", Toast.LENGTH_SHORT).show());
                } finally {
                    response.close();
                }
            }
        });
    }




    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        int sourceCurrencyPosition = sourceCurrencySpinner.getSelectedItemPosition();
        int targetCurrencyPosition = targetCurrencySpinner.getSelectedItemPosition();
        editor.putInt("SourceCurrencyPosition", sourceCurrencyPosition);
        editor.putInt("TargetCurrencyPosition", targetCurrencyPosition);

        String enteredValue = amount.getText().toString();
        editor.putString("enteredValue", enteredValue);

        editor.apply();
    }


    @Override
    protected void onResume() {
        super.onResume();

        // Wiederherstellen der Benutzereingaben (Spinner)
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        int sourceCurrencyPosition = prefs.getInt("SourceCurrencyPosition", 0);
        int targetCurrencyPosition = prefs.getInt("TargetCurrencyPosition", 0);
        sourceCurrencySpinner.setSelection(sourceCurrencyPosition);
        targetCurrencySpinner.setSelection(targetCurrencyPosition);

        String enteredValue = prefs.getString("enteredValue", "");
        amount.setText(enteredValue);




    }


        @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_main);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_main);
        }
        setupUI();
    }

    private void setupUI() {
        String[] currencyNames = database.getCurrencies();
        updateCurrencies();
        // ArrayAdapter für die Spinners erstellen
       // ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currencyNames);
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Spinner für Quellwährung und Zielwährung aus dem Layout holen und den ArrayAdapter setzen

        sourceCurrencySpinner.setAdapter(new CurrencyItemAdapter(database));
        targetCurrencySpinner.setAdapter(new CurrencyItemAdapter(database));


        calculate.setOnClickListener(v -> {
            // Get the selected currencies and amount
            String sourceCurrency = sourceCurrencySpinner.getSelectedItem().toString();
            String targetCurrency = targetCurrencySpinner.getSelectedItem().toString();
            String amountString = amount.getText().toString();

            if (!amountString.isEmpty()) {
                double amount1 = Double.parseDouble(amountString);

                // Perform currency conversion
                double convertedAmount = database.convert(amount1, sourceCurrency, targetCurrency);

                // Display the result to two decimal points
                TextView resultTextView = findViewById(R.id.resultText);
                resultTextView.setText(String.format("%.2f", convertedAmount));
                setShareText(String.format("%.2f", convertedAmount));
            } else {
                // If no amount is entered, show an error message
                Toast.makeText(MainActivity.this, "Please enter an amount.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}