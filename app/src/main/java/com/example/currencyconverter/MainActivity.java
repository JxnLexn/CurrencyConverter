package com.example.currencyconverter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    ExchangeRateDatabase database = ExchangeRateDatabase.getInstance();
    Spinner sourceCurrencySpinner;
    Spinner targetCurrencySpinner;
    EditText amount;
    Button calculate;
    private ShareActionProvider shareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sourceCurrencySpinner = findViewById(R.id.sourceSpinner);
        targetCurrencySpinner = findViewById(R.id.targetSpinner);

        amount = findViewById(R.id.inputAmount);
        calculate = findViewById(R.id.calcButton);

        Toolbar toolbar = findViewById(R.id.toolbar);
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
        if (id == R.id.my_menu_entry) {
            Intent intent = new Intent(this, CurrencyListActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.refresh_rate) {
            updateCurrenciesWorker();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateCurrenciesWorker() {
        WorkManager workManager = WorkManager.getInstance(getApplicationContext());
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ExchangeRateUpdateWorker.class).build();
        workManager.enqueue(workRequest);

        workManager.getWorkInfoByIdLiveData(workRequest.getId()).observe(this, workInfo -> {
            if (workInfo != null && workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                Toast.makeText(MainActivity.this, "Currency rates successfully updated", Toast.LENGTH_LONG).show();

                updateCurrencyAdapters();
            } else if (workInfo != null && workInfo.getState() == WorkInfo.State.FAILED) {
                Toast.makeText(MainActivity.this, "Currency rates could not be updated. Please check your connection.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateCurrencyAdapters() {
        new Handler().postDelayed(() -> {
            int sourceCurrencyPosition = sourceCurrencySpinner.getSelectedItemPosition();
            int targetCurrencyPosition = targetCurrencySpinner.getSelectedItemPosition();

            CurrencyItemAdapter newSourceAdapter = new CurrencyItemAdapter(ExchangeRateDatabase.getInstance());
            CurrencyItemAdapter newTargetAdapter = new CurrencyItemAdapter(ExchangeRateDatabase.getInstance());

            sourceCurrencySpinner.setAdapter(newSourceAdapter);
            targetCurrencySpinner.setAdapter(newTargetAdapter);

            sourceCurrencySpinner.setSelection(sourceCurrencyPosition);
            targetCurrencySpinner.setSelection(targetCurrencyPosition);
        }, 600); //
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
        updateCurrenciesWorker();
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
        updateCurrenciesWorker();
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