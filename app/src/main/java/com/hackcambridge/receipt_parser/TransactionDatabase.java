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

    private static String getFileName() {
        return "/data/data/com.hackcambridge.receipt_parser/databases" + DATABASE_NAME;
    }

  /*  public static int getTransactionCount(){
        if(database == null){
            database = SQLiteDatabase.openOrCreateDatabase(getFileName(), null);
            database.execSQL("CREATE TABLE IF NOT EXISTS transactionTable(TransID int PRIMARY KEY AUTOINCREMENT , Shop varchar, Amount int, Category int)");
            database.execSQL("INSERT INTO transCount values (0)");
        }

        String[] cols = {"COUNT(*)"};
        Cursor c = database.query("transactionTable", cols, null, null, null, null, null);
        int count = c.getInt(0);
        c.close();
        return count;
    }
*/
    //Store one Transaction
    public static void store(Transaction t){
        if(database == null){
            database = SQLiteDatabase.openOrCreateDatabase(getFileName(), null);
            database.execSQL("CREATE TABLE IF NOT EXISTS transactionTable(TransID int PRIMARY KEY AUTOINCREMENT , Shop varchar, Amount int, Category int)");
            database.execSQL("INSERT INTO transCount values (0)");
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
            database.execSQL("CREATE TABLE IF NOT EXISTS transactionTable(TransID int PRIMARY KEY AUTOINCREMENT , Shop varchar, Amount int, Category int)");
            database.execSQL("INSERT INTO transCount values (0)");
        }
        ArrayList<Transaction> list = new ArrayList<>();
        String[] cols = {"transID", "Shop", "Amount"};
        Cursor c = database.query("transactionTable", cols, null, null, null, null, "transID ");
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
