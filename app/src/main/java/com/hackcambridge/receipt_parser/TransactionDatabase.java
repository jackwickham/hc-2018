package com.hackcambridge.receipt_parser;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TransactionDatabase {

    private static final String DATABASE_NAME = "Transactions.db";
    private static SQLiteDatabase database;

    private static final String CREATE_TABLE_TRANSACTIONS = "CREATE TABLE IF NOT EXISTS transactionTable(TransID INTEGER PRIMARY KEY AUTOINCREMENT, Shop varchar, Amount int, Category int, ImagePath varchar, date INTEGER)";

    private static String getFileName() {
        String file = "/data/data/com.hackcambridge.receipt_parser/databases" + DATABASE_NAME;
        SQLiteDatabase.deleteDatabase(new java.io.File(file));
        return file;
    }

    public static void sampleData() {
        String[] shops = {"Sainsbury's", "Next", "NewLook", "WHSmiths", "Tesco", "Ikea"};
        int[] categories = {1, 2, 2, 3, 1, 0};
        Random rand = new Random();
        for (int i = 0; i < 15; i++) {
            int index = rand.nextInt(shops.length);
            Transaction tx = new Transaction(shops[index], rand.nextInt(2000) + 1000, null, categories[index], (System.currentTimeMillis() / 1000L) - rand.nextInt(604800));
            store(tx);
        }
    }

    //Store one Transaction
    public static void store(Transaction t){
        if(database == null){
            database = SQLiteDatabase.openOrCreateDatabase(getFileName(), null);
            database.execSQL(CREATE_TABLE_TRANSACTIONS);
        }

        ContentValues transactionRow = new ContentValues();
        transactionRow.put("Shop", t.getShop());
        int amount = t.getAmount();
        transactionRow.put("Amount", amount);
        transactionRow.put("Category", t.getCategory().getId());
        transactionRow.put("ImagePath", t.getImagePath());
        transactionRow.put("date", t.getDate());

        database.insert("transactionTable", null, transactionRow);
    }

    //Load all Transactions
    public static List<Transaction> load(){
        if(database == null){
            database = SQLiteDatabase.openOrCreateDatabase(getFileName(), null);
            database.execSQL(CREATE_TABLE_TRANSACTIONS);
        }
        ArrayList<Transaction> list = new ArrayList<>();
        String[] cols = {"transID", "Shop", "Amount", "ImagePath", "date", "category"};
        Cursor c = database.query("transactionTable", cols, null, null, null, null, "transID DESC");
        while(c.moveToNext()){
            String shop = c.getString(1);
            int amount = c.getInt(2);
            String imagePath = c.getString(3);
            long date = c.getLong(4);
            int category = c.getInt(5);
            Transaction t = new Transaction(shop, amount, imagePath, category, date);
            list.add(t);
        }
        c.close();
        return list;
    }

    public static List<Transaction> loadCategory(int category){
        if(database == null){
            database = SQLiteDatabase.openOrCreateDatabase(getFileName(), null);
            database.execSQL(CREATE_TABLE_TRANSACTIONS);
        }
        ArrayList<Transaction> list = new ArrayList<>();
        String[] cols = {"transID", "Shop", "Amount", "ImagePath", "date", "category"};
        String[] args = {Integer.toString(category)};
        Cursor c = database.query("transactionTable", cols, "Category = ?", args, null, null, "transID DESC");
        while(c.moveToNext()){
            String shop = c.getString(1);
            int amount = c.getInt(2);
            String imagePath = c.getString(3);
            long date = c.getLong(4);
            int cat = c.getInt(5);
            Transaction t = new Transaction(shop, amount, imagePath, cat, date);
            list.add(t);
        }
        c.close();
        return list;
    }

    public static int categoryTotals(int category){
        if(database == null){
            database = SQLiteDatabase.openOrCreateDatabase(getFileName(), null);
            database.execSQL(CREATE_TABLE_TRANSACTIONS);
        }
        String[] cols = {"transID", "SUM(Amount)", "category"};
        String[] args = {Integer.toString(category)};
        Cursor c = database.query("transactionTable", cols, "Category = ?", args, null, null, "transID DESC");
        c.moveToNext();
        int total = c.getInt(1);
        c.close();
        return total;
    }

    public static Map<Integer, Integer> graphData(){
        if(database == null){
            database = SQLiteDatabase.openOrCreateDatabase(getFileName(), null);
            database.execSQL(CREATE_TABLE_TRANSACTIONS);
        }
        String[] cols = {"transID", "Amount", "date"};
        Cursor c = database.query("transactionTable", cols, null, null, null, null, "transID DESC");
        HashMap<Integer, Integer> data = new HashMap<>();
        int today = (int)(System.currentTimeMillis() / 1000L) / 86400;
        for (int i = 0; i < 7; i++) {
            data.put(today - i, 0);
        }
        while(c.moveToNext()) {
            int amount = c.getInt(1);
            int day = c.getInt(2) / 86400;
            if(data.containsKey(day)) {
                int total = data.get(day) + amount;
                data.put(day, total);
            }
        }
        c.close();
        return data;
    }
}

