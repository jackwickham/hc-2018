package com.hackcambridge.receipt_parser;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Locale;

public class TransactionListItemHolder extends RecyclerView.ViewHolder {
	private View itemView;
	private Transaction transaction;

	private TextView shopHolder;
	private TextView amountHolder;

	public TransactionListItemHolder(View itemView) {
		super(itemView);
		this.itemView = itemView;

		shopHolder = itemView.findViewById(R.id.transaction_shop);
		amountHolder = itemView.findViewById(R.id.transaction_amount);
	}

	public void setTransaction(Transaction tx) {
		transaction = tx;

		shopHolder.setText(tx.getShop());
		amountHolder.setText(String.format(Locale.ENGLISH, "£%d.%02d", tx.getPounds(), tx.getPence()));
	}
}
