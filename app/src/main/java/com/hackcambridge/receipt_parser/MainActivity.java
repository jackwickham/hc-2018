package com.hackcambridge.receipt_parser;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.hackcambridge.cognitive.Parser;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.RandomAccess;

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
	private TransactionAdapter transactionAdapter;
	private RecyclerView categoryListView;
	private List<Transaction> categories;
	private CategoryAdapter categoryAdapter;
	private String currentImagePath;

	private boolean changePage(int selectedPage) {
		currentView.setVisibility(View.GONE);

		switch (selectedPage) {
			case R.id.navigation_transactions:
				transactionListView.setVisibility(View.VISIBLE);
				currentView = transactionListView;
				return true;
			case R.id.navigation_categories:
				categoryListView.setVisibility(View.VISIBLE);
				currentView = categoryListView;
				return true;
		}
		return false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		transactionListView = findViewById(R.id.transaction_list);
		categoryListView = findViewById(R.id.transaction_categories);
		currentView = transactionListView;

		BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
		navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

		transactions = TransactionDatabase.load();

		transactionAdapter = new TransactionAdapter();
		transactionListView.setAdapter(transactionAdapter);

		categoryAdapter = new CategoryAdapter();
		categoryListView.setAdapter(categoryAdapter);

		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
		transactionListView.setLayoutManager(layoutManager);

		RecyclerView.LayoutManager categoryLayoutManager = new LinearLayoutManager(this);
		categoryListView.setLayoutManager(categoryLayoutManager);
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
			try {
				File file = createImage();
				Uri uri = FileProvider.getUriForFile(this, "com.hackcambridge.fileprovider", file);
				cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
				startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
			}
			catch (IOException e) {
				// Ain't nobody got time for that.
			}
		}
	}

	private File createImage() throws IOException {
		String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		File storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
		File file = new File(storageDir, /*"receipts/" +*/ timestamp + ".bmp");
		this.currentImagePath = file.getAbsolutePath();
		return file;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//			Bitmap imageBitmap = (Bitmap) extras.get("data");
//			ByteArrayOutputStream stream = new ByteArrayOutputStream();
//			imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//			Thread t = new ParserThread(this, stream.toByteArray());
			BitmapFactory.Options options = new BitmapFactory.Options();
			Bitmap bmp = BitmapFactory.decodeFile(currentImagePath, options);
			options.inSampleSize = Math.min(
					options.outWidth / 3200,
					options.outHeight / 3200
			);
			bmp = BitmapFactory.decodeFile(currentImagePath, options);
			Parcel parcel = Parcel.obtain();
			bmp.writeToParcel(parcel, 0);
			Thread t = new ParserThread(this, parcel.createByteArray());
			t.start();
		}
	}

	private static class ParserThread extends Thread {
		MainActivity activity;
		byte[] buffer;
		public ParserThread(MainActivity a, byte[] b) {
			this.activity = a;
			this.buffer = b;
		}
		@Override
		public void run() {
			try {
				Parser.ExtractedData dat = Parser.parse(this.buffer);
				this.activity.addTransaction(new Transaction(dat.merchant, dat.totalValue));
				ProgressDialog dialog = showTransactionProgressDialog();
				Dialog editDialog = showTransactionEditDialog(dialog, "Sainsbury's", 2260);
				editDialog.show();
			}
			catch (IOException | JSONException e) {
				e.printStackTrace();
				// Who needs exception handling?
			}

	private ProgressDialog showTransactionProgressDialog() {
		return ProgressDialog.show(this, "Processing", "Analysing your image", true, false);
	}

	private Dialog showTransactionEditDialog(ProgressDialog prevDialog, String shopName, int amount) {
		if (prevDialog != null) {
			prevDialog.dismiss();
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Confirm Entry");

		View dialogView = getLayoutInflater().inflate(R.layout.edit_dialog, null);
		final EditText shopEntryField = ((EditText)dialogView.findViewById(R.id.shop_entry));
		final EditText amountEntryField = ((EditText)dialogView.findViewById(R.id.amount_entry));
		shopEntryField.setText(shopName);
		amountEntryField.setText("45.67");

		builder.setView(dialogView)
				.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String resultShopName = shopEntryField.getText().toString();
						String resultAmount = amountEntryField.getText().toString();

						int amount = (int) Math.round(Float.parseFloat(resultAmount) * 100.0);

						addTransaction(new Transaction(resultShopName, amount));
					}
				})
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

		return builder.create();
	}

	private void addTransaction(Transaction transaction) {
		transactions.add(0, transaction);
		transactionAdapter.notifyItemInserted(0);

		TransactionDatabase.store(transaction);
	}

	public void onCameraFabPressed(View view) {
		switchToCamera();
	}


	private class CategoryAdapter extends RecyclerView.Adapter<TransactionListItemHolder> {
		private TransactionListItemHolder txItemHolder;

		@Override
		public TransactionListItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View viewHolder = getLayoutInflater().inflate(R.layout.transaction_list_item, parent, false);
			txItemHolder = new TransactionListItemHolder(viewHolder);
			return txItemHolder;
		}

		@Override
		public void onBindViewHolder(TransactionListItemHolder holder, int position) {
			txItemHolder.setTransaction(new Transaction("Test", 1337));
		}

		@Override
		public int getItemCount() {
			return 15;
		}
	}
}
