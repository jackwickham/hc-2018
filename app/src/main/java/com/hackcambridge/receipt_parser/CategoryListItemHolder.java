package com.hackcambridge.receipt_parser;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

public class CategoryListItemHolder extends RecyclerView.ViewHolder {
	ImageView image;
	TextView name;
	TextView amount;

	public CategoryListItemHolder(View itemView) {
		super(itemView);

		image = itemView.findViewById(R.id.category_image);
		name = itemView.findViewById(R.id.category_name);
		amount = itemView.findViewById(R.id.category_total);
	}

	public void setCategory(Category category) {
		image.setImageResource(category.getIcon());
		name.setText(category.getName());
		int totalAmount = TransactionDatabase.categoryTotals(category.getId());
		amount.setText(String.format(Locale.ENGLISH, "£%d.%02d", totalAmount / 100, totalAmount % 100));
	}
}
