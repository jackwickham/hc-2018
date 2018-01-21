package com.hackcambridge.receipt_parser;


import java.util.TreeMap;

public class Merchants {
    private static TreeMap<String, Integer> merchants;

    private static void populate(){
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

    private static boolean contains(String search){
        //Clean the string
        search = search.replaceAll("[^a-zA-Z ]", "").toLowerCase();
        return merchants.containsKey(search);
    }

    private static int get(String key){
        if(!merchants.containsKey(key)) return 0;
        int val = merchants.get(key);
        return val;
    }
}
