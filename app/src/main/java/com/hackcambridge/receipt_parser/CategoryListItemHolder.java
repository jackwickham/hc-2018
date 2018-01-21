package com.hackcambridge.receipt_parser;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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
		// TODO: Amount
	}
}
