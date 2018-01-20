package com.hackcambridge.cognitive;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.ByteBuffer;
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

        String merchant = "Unknown";
        int value = 0;

        JSONArray arr = obj.getJSONArray("regions");
        for (int i = 0; i < arr.length(); ++i) {
            JSONObject o = arr.getJSONObject(i);
            JSONArray lines = o.getJSONArray("lines");
            for (int j = 0; j < lines.length(); ++j) {
                JSONObject line = lines.getJSONObject(j);
                JSONArray words = line.getJSONArray("words");
                for (int k = 0; k < words.length(); ++k) {
                    String word = words.getJSONObject(k).getString("text");
                    Pattern pattern = Pattern.compile("[$|£|€]?(\\d+)\\.(\\d{2})");
                    Matcher matcher = pattern.matcher(word);
                    if (matcher.find()) {
                        int pounds = Integer.parseInt(matcher.group(1));
                        int pence = Integer.parseInt(matcher.group(2));
                        int thisValue = pounds*100 + pence;
                        if (thisValue > value) value = thisValue;
                    }
                }
            }
        }

        return new ExtractedData(merchant, value);
    }

}
