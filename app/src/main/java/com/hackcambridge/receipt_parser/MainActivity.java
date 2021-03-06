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
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.hackcambridge.cognitive.Parser;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
	public static final int REQUEST_IMAGE_CAPTURE = 1;

	private static final int MSG_SUCCESS = 1;
	private static final int MSG_FAILURE = 2;

	private static final String TAG = "MainActivity";

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
	private LinearLayout graphView;
	private String currentImagePath;
	private FloatingActionButton add_new_button;

	private boolean changePage(int selectedPage) {
		currentView.setVisibility(View.GONE);

		switch (selectedPage) {
			case R.id.navigation_transactions:
				transactionListView.setVisibility(View.VISIBLE);
				add_new_button.setVisibility(View.VISIBLE);
				currentView = transactionListView;
				return true;
			case R.id.navigation_categories:
				categoryListView.setVisibility(View.VISIBLE);
				add_new_button.setVisibility(View.GONE);
				currentView = categoryListView;
				return true;
			case R.id.navigation_graph:
				graphView.setVisibility(View.VISIBLE);
				add_new_button.setVisibility(View.GONE);
				graphView.removeAllViews();
				graphView.addView(getGraphView());
				currentView = graphView;
				return true;
		}
		return false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		add_new_button = findViewById(R.id.add_new);

		transactionListView = findViewById(R.id.transaction_list);
		categoryListView = findViewById(R.id.transaction_categories);
		graphView = findViewById(R.id.transaction_graph);
		currentView = transactionListView;

		BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
		navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

		transactions = TransactionDatabase.load();

		transactionAdapter = new TransactionAdapter();
		transactionListView.setAdapter(transactionAdapter);

		categoryAdapter = new CategoryAdapter();
		categoryListView.setAdapter(categoryAdapter);

		graphView.addView(getGraphView());

		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
		transactionListView.setLayoutManager(layoutManager);

		RecyclerView.LayoutManager categoryLayoutManager = new GridLayoutManager(this, 2);
		categoryListView.setLayoutManager(categoryLayoutManager);
	}

	public GraphView getGraphView() {
		Map<Integer, Integer> data = TransactionDatabase.graphData();
		List<DataPoint> points = new ArrayList<>();
		double maxY = 1000.0;
		int minX = Integer.MAX_VALUE;
		int maxX = 0;
		for (Map.Entry<Integer, Integer> entry : data.entrySet()) {
			points.add(new DataPoint(entry.getKey(), entry.getValue()));
			maxY = Math.max(maxY, Math.ceil(entry.getValue() / 1000.0) * 1000);
			minX = Math.min(minX, entry.getKey());
			maxX = Math.max(maxX, entry.getKey());
		}
		DataPoint[] pointsArr = new DataPoint[points.size()];
		points.toArray(pointsArr);
		BarGraphSeries<DataPoint> series = new BarGraphSeries<>(pointsArr);
		series.setSpacing(50);
		GraphView graphView = new GraphView(this);
		graphView.addSeries(series);
		graphView.getGridLabelRenderer().setLabelFormatter(new StaticLabelsFormatter(graphView) {
			@Override
			public String formatLabel(double value, boolean isValueX) {
				if (isValueX) {
					// show day for x values
					final String[] days = {"Thu", "Fri", "Sat", "Sun", "Mon", "Tue", "Wed"};
					return days[(int) value % 7];//super.formatLabel(value, isValueX);
				} else {
					// show currency for y values
					return "£" + super.formatLabel(value/100, isValueX);
				}
			}
		});
		Viewport vp = graphView.getViewport();
		vp.setYAxisBoundsManual(true);
		vp.setMinY(0.0);
		vp.setMaxY(maxY);
		vp.setXAxisBoundsManual(true);
		vp.setMinX(minX - 1);
		vp.setMaxX(maxX + 1);
		return graphView;
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

			final ProgressDialog processingDialog = showTransactionProgressDialog();
			Handler h = new Handler(Looper.getMainLooper()) {
				@Override
				public void handleMessage(Message msg) {
					processingDialog.dismiss();
					if (msg.what == MSG_SUCCESS) {
						Parser.ExtractedData obj = (Parser.ExtractedData) msg.obj;
						Dialog editDialog = buildTransactionEditDialog(obj.merchant, obj.totalValue);
						editDialog.show();
					} else {
						// Handle error
						// TODO: Allow them to enter it manually instead
						final AlertDialog.Builder errorDialog = new AlertDialog.Builder(MainActivity.this);
						errorDialog.setTitle(R.string.processing_error_title);
						errorDialog.setMessage(R.string.processing_error_message);
						errorDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();
							}
						});
						errorDialog.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								switchToCamera();
							}
						});
                        errorDialog.setNeutralButton(R.string.enter_manually, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int i) {
                                        dialog.dismiss();
                                        Dialog editDialog = buildTransactionEditDialog(null, 0);
                                        editDialog.show();
                                    }
                                }

                        );

						errorDialog.show();
					}
				}
			};

//			Bitmap imageBitmap = (Bitmap) extras.get("data");
//			ByteArrayOutputStream stream = new ByteArrayOutputStream();
//			imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//			Thread t = new ParserThread(this, stream.toByteArray());

			BitmapFactory.Options options = new BitmapFactory.Options();
			BitmapFactory.decodeFile(currentImagePath, options);
			options.inSampleSize = Math.max(
					(int) Math.pow(2, Math.ceil(Math.log(options.outWidth / 3200.0) / Math.log(2))),
					(int) Math.pow(2, Math.ceil(Math.log(options.outHeight / 3200.0) / Math.log(2)))
			);
			options.inJustDecodeBounds = false;
			Bitmap bmp = BitmapFactory.decodeFile(currentImagePath, options);

			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);

			Thread t = new ParserThread(stream.toByteArray(), h);
			t.start();

		}
	}

	private class ParserThread extends Thread {
		byte[] buffer;
		Handler handler;

		public ParserThread(byte[] b, Handler h) {
			this.buffer = b;
			this.handler = h;
		}

		@Override
		public void run() {
			Message m = Message.obtain(this.handler);
			try {
				Parser.MerchantDbLookup callback = new Parser.MerchantDbLookup() {
					@Override
					public boolean lookup(String merchant) {
						return Merchants.contains(merchant);
					}
				};
				m.obj = Parser.parse(this.buffer, callback);
				m.what = MSG_SUCCESS;
			} catch (IOException | JSONException e) {
				Log.e(TAG, "Error", e);
				// Who needs exception handling?
				m.what = MSG_FAILURE;
			}
			m.sendToTarget();
		}
	}

	private ProgressDialog showTransactionProgressDialog() {
		return ProgressDialog.show(this, "Processing", "Analysing your image", true, false);
	}

	private static String formatAmount(int amount) {
		return String.format(Locale.ENGLISH, "%d.%02d", amount / 100, amount % 100);
	}

	private Category editDialogSelectedCategory;

	private Dialog buildTransactionEditDialog(String shopName, int amount) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Confirm Entry");

		View dialogView = getLayoutInflater().inflate(R.layout.edit_dialog, null);
		final EditText shopEntryField = ((EditText)dialogView.findViewById(R.id.shop_entry));
		final EditText amountEntryField = ((EditText)dialogView.findViewById(R.id.amount_entry));
		final TextInputLayout shopEntryLayout = dialogView.findViewById(R.id.shop_entry_layout);
		final TextInputLayout amountEntryLayout = dialogView.findViewById(R.id.amount_entry_layout);
		final Spinner categorySpinner = dialogView.findViewById(R.id.category_entry);
		shopEntryField.setText(shopName);
		if(amount != 0)	amountEntryField.setText(formatAmount(amount));
		final SpinnerAdapter categorySpinnerAdapter = Category.getSpinnerAdapter(this);
		categorySpinner.setAdapter(categorySpinnerAdapter);

		editDialogSelectedCategory = Category.get(Merchants.get(shopName));
		categorySpinner.setSelection(editDialogSelectedCategory.getId());
		categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				editDialogSelectedCategory = Category.get(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				editDialogSelectedCategory = Category.get(0);
			}
		});

		builder.setView(dialogView)
				.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String resultShopName = shopEntryField.getText().toString();
						String resultAmount = amountEntryField.getText().toString();

						try {
                            int amount = (int) Math.round(Float.parseFloat(resultAmount) * 100.0);
                            addTransaction(new Transaction(resultShopName, amount, currentImagePath, editDialogSelectedCategory.getId()));
                        } catch (NumberFormatException e){
                            amountEntryLayout.setError("Please enter a number");
							Snackbar sb = Snackbar.make(currentView, "Invalid amount", BaseTransientBottomBar.LENGTH_SHORT);
							sb.show();
                        }

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
		Merchants.populate(transactions);
		categoryAdapter.notifyItemChanged(transaction.getCategory().getId());
	}

	public void onCameraFabPressed(View view) {
		switchToCamera();
	}


	private class CategoryAdapter extends RecyclerView.Adapter<CategoryListItemHolder> {
		private CategoryListItemHolder itemHolder;

		@Override
		public CategoryListItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View viewHolder = getLayoutInflater().inflate(R.layout.category_list_item, parent, false);
			itemHolder = new CategoryListItemHolder(viewHolder);
			return itemHolder;
		}

		@Override
		public void onBindViewHolder(CategoryListItemHolder holder, int position) {
			itemHolder.setCategory(Category.get(position));
		}

		@Override
		public int getItemCount() {
			return Category.numCategories();
		}
	}
}
