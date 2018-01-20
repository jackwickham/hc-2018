package com.hackcambridge.receipt_parser;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.MonthDisplayHelper;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class TransactionDatabase {

    private static final String DATABASE_NAME = "Transactions.db";
    private static SQLiteDatabase database;

    private static final String CREATE_TABLE_TRANSACTIONS = "CREATE TABLE IF NOT EXISTS transactionTable(TransID INTEGER PRIMARY KEY AUTOINCREMENT , Shop varchar, Amount int, Category int)";

    private static String getFileName() {
        String file = "/data/data/com.hackcambridge.receipt_parser/databases" + DATABASE_NAME;
        // SQLiteDatabase.deleteDatabase(new File(file));
        return file;
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
        transactionRow.put("Category", 0);

        database.insert("transactionTable", null, transactionRow);
    }

    //Load all Transactions
    public static List<Transaction> load(){
        if(database == null){
            database = SQLiteDatabase.openOrCreateDatabase(getFileName(), null);
            database.execSQL(CREATE_TABLE_TRANSACTIONS);
        }
        ArrayList<Transaction> list = new ArrayList<>();
        String[] cols = {"transID", "Shop", "Amount"};
        Cursor c = database.query("transactionTable", cols, null, null, null, null, "transID DESC");
        while(c.moveToNext()){
            String shop = c.getString(1);
            int amount = c.getInt(2);
            Transaction t = new Transaction(shop, amount);
            list.add(t);
        }
        c.close();
        return list;
    }
}
