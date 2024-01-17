package com.example.currencyconverter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class CurrencyListAdapter extends BaseAdapter {

    private final List<CurrencyEntry> data;

    public CurrencyListAdapter(List<CurrencyEntry> data) {
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        Context context = viewGroup.getContext();
        CurrencyEntry entry = data.get(position);

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_view_item, null, false);
        }

        TextView currencyNameTextView = view.findViewById(R.id.text_currency_name);
        TextView exchangeRateTextView = view.findViewById(R.id.text_exchange_rate);

        // Setze die Daten in die gefundenen TextViews
        if (currencyNameTextView != null) {
            currencyNameTextView.setText(entry.getCurrencyName());
        }

        if (exchangeRateTextView != null) {
            // Formatieren des Wechselkurses z.B. als String mit zwei Dezimalstellen
            @SuppressLint("DefaultLocale") String formattedExchangeRate = String.format("%.2f", entry.getExchangeRate());
            exchangeRateTextView.setText(formattedExchangeRate);
        }

        return view;
    }
}
