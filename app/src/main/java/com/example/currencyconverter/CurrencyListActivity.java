package com.example.currencyconverter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CurrencyListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_list);

        ExchangeRateDatabase database = new ExchangeRateDatabase();
        /* String[] currencyNames = database.getCurrencies();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.list_view_item, R.id.text_view, currencyNames);

        ListView listView = (ListView)findViewById(R.id.listViewCurrencies);
        listView.setAdapter(arrayAdapter);

         */

        // Erhalte die Währungsdaten aus der ExchangeRateDatabase
        List<CurrencyEntry> currencyEntries = new ArrayList<>();
        String[] currencyNames = database.getCurrencies();

        // Füge für jede Währung einen CurrencyEntry mit dem Währungsnamen und dem Wechselkurs hinzu
        for (String currency : currencyNames) {
            double exchangeRate = database.getExchangeRate(currency);
            currencyEntries.add(new CurrencyEntry(currency, exchangeRate));
        }

        // CurrencyListAdapter adapter = new CurrencyListAdapter(currencyEntries);

        ListView listView = findViewById(R.id.listViewCurrencies);
        listView.setAdapter(new CurrencyItemAdapter(database));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String currency = (String)parent.getItemAtPosition(position);
                String capital = database.getCapital(currency);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + capital));
                startActivity(intent);
            }
        });
    }
}
