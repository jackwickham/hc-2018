package com.hackcambridge.cognitive;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    public static class ExtractedData {
        public String merchant;
        public int totalValue;
        public ExtractedData(String m, int v) {
            merchant = m;
            totalValue = v;
        }
    }

    public static ExtractedData parse(byte[] buffer) throws IOException, JSONException {
        Endpoint endpoint = new Endpoint();
        JSONObject obj = endpoint.post(buffer);
        Log.i("info", obj.toString(4));

        String merchant = "";
        WordObj lastTotal = null;
        List<WordObj> amounts = new ArrayList<>();

        StringBuilder toPrint = new StringBuilder();

        JSONArray arr = obj.getJSONArray("regions");
        for (int i = 0; i < arr.length(); ++i) {
            JSONObject o = arr.getJSONObject(i);
            JSONArray lines = o.getJSONArray("lines");
            for (int j = 0; j < lines.length(); ++j) {
                toPrint.append("\n");
                JSONObject line = lines.getJSONObject(j);
                JSONArray words = line.getJSONArray("words");
                for (int k = 0; k < words.length(); ++k) {
                    WordObj wordObj = new WordObj(words.getJSONObject(k));
                    if (wordObj.isTotal()) lastTotal = wordObj;
                    else if (wordObj.isAmount()) amounts.add(wordObj);
                }
            }
        }

        int value = 0;

        if (amounts.size() > 0) {
            value = amounts.get(amounts.size() - 1).amount;
        }

        if (lastTotal != null) {
            int minDist = Integer.MAX_VALUE;
            for (WordObj amount : amounts) {
                int diffX = Math.abs(lastTotal.x - amount.x);
                int diffY = Math.abs(lastTotal.y - amount.y);
                int dist = (diffX*diffX + diffY*diffY);
                if (dist < minDist) {
                    minDist = dist;
                    value = amount.amount;
                }
            }
        }

        return new ExtractedData(merchant, value);
    }

    private static class WordObj {

        public String value;
        public int x;
        public int y;
        public int amount = 0;

        public WordObj(JSONObject word) throws JSONException {
            value = word.getString("text");
            String[] coords = word.getString("boundingBox").split(",");
            x = Integer.parseInt(coords[0]) + (Integer.parseInt(coords[2]) / 2);
            y = Integer.parseInt(coords[1]) + (Integer.parseInt(coords[3]) / 2);
        }

        public boolean isTotal() {
            Pattern p = Pattern.compile("TOTAL");
            Matcher m = p.matcher(value);
            return m.find();
        }

        public boolean isAmount() {
            Pattern p = Pattern.compile("^[$£€]?(\\d+)\\.(\\d{2})p?$");
            Matcher m = p.matcher(value);
            if (m.find()) {
                int pounds = Integer.parseInt(m.group(1));
                int pence = Integer.parseInt(m.group(2));
                amount = pounds*100 + pence;
                return true;
            }
            return false;
        }

    }

}
