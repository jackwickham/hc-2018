package com.hackcambridge.receipt_parser;

import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
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

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
	public static final int REQUEST_IMAGE_CAPTURE = 1;

	private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
			= new BottomNavigationView.OnNavigationItemSelectedListener() {

		@Override
		public boolean onNavigationItemSelected(@NonNull MenuItem item) {
			return changePage(item.getItemId());
		}
	};

	private View currentView;
	private RecyclerView transactionListView;
	private List<Transaction> transactions;
	private TransactionAdapter adapter;

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

		transactions = TransactionDatabase.load();

		adapter = new TransactionAdapter();
		transactionListView.setAdapter(adapter);

		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
		transactionListView.setLayoutManager(layoutManager);
	}

	private class TransactionAdapter extends RecyclerView.Adapter<TransactionListItemHolder> {
		private TransactionListItemHolder txItemHolder;

		@Override
		public TransactionListItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View viewHolder = getLayoutInflater().inflate(R.layout.transaction_list_item, parent, false);
			txItemHolder = new TransactionListItemHolder(viewHolder);
			return txItemHolder;
		}

		@Override
		public void onBindViewHolder(TransactionListItemHolder holder, int position) {
			Transaction tx = transactions.get(position);
			txItemHolder.setTransaction(tx);
		}

		@Override
		public int getItemCount() {
			return transactions.size();
		}
	}

	private void switchToCamera() {
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (cameraIntent.resolveActivity(getPackageManager()) != null) {
			startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
			Bundle extras = data.getExtras();
			Bitmap imageBitmap = (Bitmap) extras.get("data");
			Buffer buf = ByteBuffer.allocate(imageBitmap.getByteCount());
			imageBitmap.copyPixelsToBuffer(buf);

			// Do something

			addTransaction(new Transaction("Sainsburys", 2260));
		}
	}

	private void addTransaction(Transaction transaction) {
		transactions.add(0, transaction);
		adapter.notifyItemInserted(0);
		TransactionDatabase.store(transaction);
	}

	public void onCameraFabPressed(View view) {
		switchToCamera();
	}
}
