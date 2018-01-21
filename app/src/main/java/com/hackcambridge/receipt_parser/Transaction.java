package com.hackcambridge.receipt_parser;

/**
 * Created by jack on 20/01/18.
 */

public class Transaction {
	private String shop;
	private int amount;
	private int category;
	private String imagePath;

	public Transaction(String shop, int amount, String imagePath) {
		this.shop = shop;
		this.amount = amount;
		this.imagePath = imagePath;
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

	public int getCategory() {
	    return category;
    }

	public String getImagePath() {
	    return imagePath;
    }
}
