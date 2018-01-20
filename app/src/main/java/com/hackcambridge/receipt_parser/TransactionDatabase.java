package com.hackcambridge.receipt_parser;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.MonthDisplayHelper;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class TransactionDatabase {

    private static final String DATABASE_NAME = "Transactions.db";
    private static SQLiteDatabase database;

    public static int getTransactionCount(){
        if(database == null){
            database = SQLiteDatabase.openOrCreateDatabase(DATABASE_NAME, null);
            database.execSQL("CREATE TABLE IF NOT EXISTS transactionTable(TransID int, Shop varchar, Amount int, Category int)");
            database.execSQL("CREATE TABLE IF NOT EXISTS transCount(Count int)");
            database.execSQL("INSERT INTO transCount values (0)");
        }

        Cursor c = database.query("transCount", null, null, null, null, null, null);
        int count = c.getInt(0);
        c.close();
        return count;
    }

    //Store one Transaction
    public static void store(Transaction t){
        if(database == null){
            database = SQLiteDatabase.openOrCreateDatabase(DATABASE_NAME, null);
            database.execSQL("CREATE TABLE IF NOT EXISTS transactionTable(TransID int, Shop varchar, Amount int, Category int)");
            database.execSQL("CREATE TABLE IF NOT EXISTS transCount(Count int)");
            database.execSQL("INSERT INTO transCount values (0)");
        }
        database.execSQL("UPDATE transCount SET Count = Count + 1");
        int transID = getTransactionCount();

        ContentValues transactionRow = new ContentValues();
        transactionRow.put("TransID", transID);
        transactionRow.put("Shop", t.getShop());
        int amount = t.getPounds();
        amount *= 100;
        amount += t.getPence();
        transactionRow.put("Amount", amount);
        transactionRow.put("Category", 0);

        database.insert("transactionTable", null, transactionRow);
    }

    //Load all Transactions
    public static List<Transaction> load(){
        if(database == null){
            database = SQLiteDatabase.openOrCreateDatabase(DATABASE_NAME, null);
            database.execSQL("CREATE TABLE IF NOT EXISTS transactionTable(TransID int, Shop varchar, Amount int, Category int)");
            database.execSQL("CREATE TABLE IF NOT EXISTS transCount(Count int)");
            database.execSQL("INSERT INTO transCount values (0)");
        }
        ArrayList<Transaction> list = new ArrayList<>();
        String[] cols = {"transID", "Shop", "Amount"};
        Cursor c = database.query("transactionTable", cols, null, null, null, null, "transID ");
        while(!c.isAfterLast()){
            String shop = c.getString(1);
            int amount = c.getInt(2);
            Transaction t = new Transaction(shop, amount);
            list.add(t);
        }
        c.close();
        return list;
    }
}
