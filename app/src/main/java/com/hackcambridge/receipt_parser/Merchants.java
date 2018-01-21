package com.hackcambridge.receipt_parser;


import android.widget.TableRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

public class Merchants {
    private static TreeMap<String, Integer> merchants;

    static{
        merchants = new TreeMap<>();

        merchants.put("sainsburys", 1);
        merchants.put("tesco", 1);
        merchants.put("asda", 1);


        merchants.put("next", 2);
        merchants.put("new look", 2);
        merchants.put("tk maxx", 2);
        merchants.put("debenhams", 2);

        merchants.put("whsmith", 3);
    }

    public static void populate(List<Transaction> transactions){
        for(Transaction t : transactions){
            String shop = normalise(t.getShop());
            int category = t.getCategory().getId();
            if(contains(shop)){
                if(!(get(shop) == category)){
                    merchants.put(shop, 0);
                }
            } else {
                merchants.put(shop, category);
            }
        }
    }

    public static boolean contains(String search){
        //Clean the string
        search = normalise(search);
        return merchants.containsKey(search);
    }

    public static String search(String search){
        search = normalise(search);
        List<String> keys = new ArrayList<>(merchants.keySet());
        int i = Collections.binarySearch(keys, search);
        String result = keys.get(i + 1);
        return result;
    }

    public static String normalise(String s){
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    public static int get(String key){
        key = normalise(key);
        if(!merchants.containsKey(key)) return 0;
        int val = merchants.get(key);
        return val;
    }
}
