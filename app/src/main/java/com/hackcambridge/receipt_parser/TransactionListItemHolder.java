package com.hackcambridge.receipt_parser;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class TransactionListItemHolder extends RecyclerView.ViewHolder {
	private View itemView;

	public TransactionListItemHolder(View itemView) {
		super(itemView);
		this.itemView = itemView;
	}
}
