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

	private static ExtractedData process(List<JSONObject> regions, MerchantDbLookup callback) {

        String merchant = "";
        WordObj lastTotal = null;
        List<WordObj> amounts = new ArrayList<>();

        for (JSONObject o : regions) {
            JSONArray lines = o.getJSONArray("lines");
            for (int j = 0; j < lines.length(); ++j) {
                JSONObject line = lines.getJSONObject(j);
                JSONArray words = line.getJSONArray("words");
                for (int k = 0; k < words.length(); ++k) {
                    WordObj wordObj = new WordObj(words.getJSONObject(k));
                    if (wordObj.isTotal()) {
						lastTotal = wordObj;
					}
                    else if (wordObj.isAmount()) {
						amounts.add(wordObj);
					}
                    else if (merchant.equals("")) {
                        if (callback.lookup(wordObj.value)) {
                            merchant = wordObj.value;
                        }
                    }
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

	public static ExtractedData parse(Bitmap hires, MerchantDbLookup callback) throws IOException, JSONException {
		
		Endpoint endpoint = new Endpoint();

		final int maxWidth = 3200;
		final int maxHeight = 3200;
		final int gridStepWidth = maxWidth * 0.8;
		final int gridStepHeight = maxHeight * 0.8;

		int gridWidth = Math.ceil(((double)hires.getWidth()) / gridStepWidth);
		int gridHeight = Math.ceil(((double)hires.getHeight()) / gridStepHeight);
		
		List<JSONObject> uberregion = new ArrayList<>();
		for (int y = 0; y < gridHeight; ++y) {
			for (int x = 0; x < gridWidth; ++x) {
				Bitmap tmp = Bitmap.createBitmap(hires, x * gridStepWidth, y * gridStepHeight, maxWidth, maxHeight);
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				tmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
				try {
					JSONObject obj = endpoint.post(stream.toByteArray());
					JSONArray arr = obj.getJSONArray("regions");
					for (int i = 0; i < arr.length(); ++i) {
						uberregion.add(arr.getJSONObject(i));
					}
				}
				catch (IOException | JSONException e) {
					Log.e("err", "An error occured", e);
				}
			}
		}

		return process(uberregion, callback);

	}

    public static ExtractedData parse(byte[] buffer, MerchantDbLookup callback) throws IOException, JSONException {
        Endpoint endpoint = new Endpoint();
        JSONObject obj = endpoint.post(buffer);
        Log.i("info", obj.toString(4));

        String merchant = "";
        WordObj lastTotal = null;
        List<WordObj> amounts = new ArrayList<>();

        JSONArray arr = obj.getJSONArray("regions");
		List<JSONObject> regions = new ArrayList<>();
		for (int i = 0; i < arr.length(); ++i) {
			regions.add(arr.getJSONObject(i));
		}

		return process(regions, callback);
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

    public interface MerchantDbLookup {
        public boolean lookup(String merchant);
    }

}
