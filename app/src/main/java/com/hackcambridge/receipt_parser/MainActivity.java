package com.hackcambridge.receipt_parser;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

	private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
			= new BottomNavigationView.OnNavigationItemSelectedListener() {

		@Override
		public boolean onNavigationItemSelected(@NonNull MenuItem item) {
			return changePage(item.getItemId());
		}
	};

	private View currentView;
	private RecyclerView transactionListView;

	private boolean changePage(int selectedPage) {
		currentView.setVisibility(View.INVISIBLE);

		switch (selectedPage) {
			case R.id.navigation_transactions:
				transactionListView.setVisibility(View.VISIBLE);
				currentView = transactionListView;
				return true;
			case R.id.navigation_categories:
				return true;
		}
		return false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		transactionListView = findViewById(R.id.transaction_list);
		currentView = transactionListView;

		BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
		navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

		transactionListView.setAdapter(new TransactionAdapter());

		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
		transactionListView.setLayoutManager(layoutManager);

	}

	private class TransactionAdapter extends RecyclerView.Adapter<TransactionListItemHolder> {
		@Override
		public TransactionListItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View viewHolder = getLayoutInflater().inflate(R.layout.transaction_list_item, parent, false);
			return new TransactionListItemHolder(viewHolder);
		}

		@Override
		public void onBindViewHolder(TransactionListItemHolder holder, int position) {

		}

		@Override
		public int getItemCount() {
			return 25;
		}
	}

}
