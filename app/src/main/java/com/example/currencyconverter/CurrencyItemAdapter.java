package com.example.currencyconverter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

public class CurrencyItemAdapter extends BaseAdapter {
    ExchangeRateDatabase rateDb;
    private List<String> currencyNames;

    public CurrencyItemAdapter(ExchangeRateDatabase db) {
        rateDb = db;
        this.currencyNames = Arrays.asList(rateDb.getCurrencies());
    }

    @Override
    public int getCount() {
        return currencyNames.size();
    }

    @Override
    public Object getItem(int position) {
        return currencyNames.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Context context = parent.getContext();
        String currencyName = rateDb.getCurrencies()[position];

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.currencies_list_view_entry, null, false);
        }

        String currencyCode = currencyNames.get(position);

        // ImageView für das Flaggenbild
        ImageView flagImageView = convertView.findViewById(R.id.image_flag);
        int flagResId = context.getResources().getIdentifier(
                "flag_" + currencyCode.toLowerCase(), "drawable", context.getPackageName());
        flagImageView.setImageResource(flagResId);

        // TextView für den Währungsnamen
        TextView currencyNameTextView = convertView.findViewById(R.id.text_currency_name);
        currencyNameTextView.setText(currencyCode);

        // TextView für den Wechselkurs
        TextView exchangeRateTextView = convertView.findViewById(R.id.text_exchange_rate);
        double exchangeRate = rateDb.getExchangeRate(currencyCode);
        exchangeRateTextView.setText(String.format("%.2f", exchangeRate));

        return convertView;
    }
}
