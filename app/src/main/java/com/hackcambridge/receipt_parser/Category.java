package com.hackcambridge.receipt_parser;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import java.util.ArrayList;
import java.util.List;

public class Category {
	private int id;
	private String name;
	private int icon;

	private Category(int id, String name, int icon) {
		this.id = id;
		this.name = name;
		this.icon = icon;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getIcon() {
		return icon;
	}

	private static List<Category> instances = new ArrayList<>(4);
	static {
		instances.add(new Category(0, "General", R.drawable.ic_group_work_black_24dp));
		instances.add(new Category(1, "Food", R.drawable.ic_restaurant_black_24dp));
		instances.add(new Category(2, "Clothing", R.drawable.ic_face_black_24dp));
		instances.add(new Category(3, "Paper goods", R.drawable.ic_book_black_24dp));
	}

	public static Category get(int id) {
		return instances.get(id);
	}

	public static SpinnerAdapter getSpinnerAdapter(Context context) {
		ArrayAdapter<Category> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, instances);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		return adapter;
	}

	@Override
	public String toString() {
		return name;
	}
}
