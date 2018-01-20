package com.hackcambridge.receipt_parser;

import android.content.Intent;
import android.database.DataSetObserver;
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
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
	private TransactionAdapter adapter;
	private String currentImagePath;

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

		transactions = new ArrayList<>(15);
		for (int i = 0; i < 15; i++) {
			transactions.add(new Transaction("Sainsburys'", 2000));
		}

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
			}
			catch (IOException | JSONException e) {
				e.printStackTrace();
				// Who needs exception handling?
			}
		}
	}

	private void addTransaction(Transaction transaction) {
		transactions.add(0, transaction);
		adapter.notifyItemInserted(0);
	}

	public void onCameraFabPressed(View view) {
		switchToCamera();
	}
}
