package com.hackcambridge.receipt_parser;


import android.widget.TableRow;

import java.util.List;
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
            String shop = t.getShop();
            int category = t.getCategory();
            if(contains(shop)){
                if(!(get(shop) == category)){
                    shop = shop.replaceAll("[^a-zA-Z ]","").toLowerCase();
                    merchants.put(shop, 0);
                }
            } else {
                shop = shop.replaceAll("[^a-zA-Z ]", "").toLowerCase();
                merchants.put(shop, category);
            }
        }
    }

    public static boolean contains(String search){
        //Clean the string
        search = search.replaceAll("[^a-zA-Z ]", "").toLowerCase();
        return merchants.containsKey(search);
    }

    public static int get(String key){
        key = key.replace("[^a-zA-Z ]","").toLowerCase();
        if(!merchants.containsKey(key)) return 0;
        int val = merchants.get(key);
        return val;
    }
}
