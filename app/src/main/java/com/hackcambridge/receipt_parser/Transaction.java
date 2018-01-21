package com.hackcambridge.receipt_parser;

/**
 * Created by jack on 20/01/18.
 */

public class Transaction {
	private String shop;
	private int amount;
	private int category;
	private String imagePath;
	private long date;

	public Transaction(String shop, int amount, String imagePath, int category) {
		this(shop, amount, imagePath,  category, System.currentTimeMillis() / 1000L);
	}
	public Transaction(String shop, int amount, String imagePath, int category, long date) {
		this.shop = shop;
		this.amount = amount;
		this.imagePath = imagePath;
		this.category = category;
		this.date = date;
	}

	public String getShop() {
		return shop;
	}

	public int getPence() {
		return amount % 100;
	}
	public int getPounds() {
		return amount / 100;
	}

	public int getAmount() {
		return amount;
	}

	public String getImagePath() {
	    return imagePath;
    }

    public long getDate() {
		return date;
	}

	public Category getCategory() {
		return Category.get(this.category);
	}
}
