package com.hackcambridge.receipt_parser;

/**
 * Created by jack on 20/01/18.
 */

public class Transaction {
	private String shop;
	private int amount;
	private int category;

	public Transaction(String shop, int amount) {
		this.shop = shop;
		this.amount = amount;
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
}
