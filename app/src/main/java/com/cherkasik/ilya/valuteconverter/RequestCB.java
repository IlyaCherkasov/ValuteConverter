package com.cherkasik.ilya.valuteconverter;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class RequestCB {
    private RequestQueue mQueue;

    void error(String name, Context context){
        Toast.makeText(context, "Something wrong with " + name, Toast.LENGTH_SHORT).show();
    }
    void getValutes(final Context context, final Spinner spinnerFrom, final Spinner spinnerTo) {
        mQueue = Volley.newRequestQueue(context);
        String url = "https://www.cbr-xml-daily.ru/daily_json.js";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    List<String> list = new ArrayList<>();
                    list. add("Российский рубль");
                    JSONObject jsonObject = response.getJSONObject("Valute");
                    Iterator<String> keys = jsonObject.keys();
                    while (keys.hasNext()){
                        String key = keys.next();
                        JSONObject valute = jsonObject.getJSONObject(key);
                        list.add(valute.getString("Name"));
                    }
                    addItemsOnSpinner(list, spinnerFrom, spinnerTo, context);

                } catch (JSONException e) {
                    error("json response", context);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                error("request", context);
            }
        });
        mQueue.add(request);
    }

    private void addItemsOnSpinner(List<String> list, Spinner spinnerFrom, Spinner spinnerTo, Context context){
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrom.setAdapter(dataAdapter);
        spinnerTo.setAdapter(dataAdapter);
    }

    void convertRequest(final Context context, String today, final String curFrom, final String curTo,
                        final Float inputVal, final String curDate, final TextView resText, final DatabaseDAO databaseDAO){
        String url;
        if (!curDate.equals(today)) {
            url = "https://www.cbr-xml-daily.ru/archive/" + curDate + "/daily_json.js";
        } else {
            url = "https://www.cbr-xml-daily.ru/daily_json.js";
        }
        mQueue = Volley.newRequestQueue(context);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject jsonObject = response.getJSONObject("Valute");
                    Iterator<String> keys = jsonObject.keys();
                    Float result = inputVal;
                    while (keys.hasNext()){
                        String key = keys.next();
                        JSONObject valute = jsonObject.getJSONObject(key);
                        if (curFrom.equals(valute.getString("Name"))){
                            result = result * Float.valueOf(valute.getString("Value"));
                        }
                        if (curTo.equals(valute.getString("Name"))){
                            result = result / Float.valueOf(valute.getString("Value"));
                        }
                    }
                    resText.setText(String.valueOf(result));
                    HistoryObject historyObject = new HistoryObject(curFrom, curTo, inputVal, result, curDate);
                    databaseDAO.addHistory(historyObject);
                } catch (JSONException e) {
                    error("response", context);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error("date, try another one", context);
            }
        });
        mQueue.add(request);
    }
}
