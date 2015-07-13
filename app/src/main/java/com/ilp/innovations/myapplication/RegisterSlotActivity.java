package com.ilp.innovations.myapplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ilp.innovations.myapplication.Beans.Slot;
import com.ilp.innovations.myapplication.Services.BookingUpdaterService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RegisterSlotActivity extends Activity {

    private EditText txtSlotId;
    private EditText txtEmpId;
    private EditText txtVehNum;
    private Button btnBook;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_slot);

        txtSlotId = (EditText) findViewById(R.id.slotId);
        txtEmpId = (EditText) findViewById(R.id.empId);
        txtVehNum = (EditText) findViewById(R.id.vehicleNum);
        btnBook = (Button) findViewById(R.id.btnBook);

        Intent intent = getIntent();
        String slot = intent.getStringExtra("slotId");
        if(slot!=null) {
            txtSlotId.setText(slot);
            txtSlotId.setEnabled(false);
        }
        else {
            finish();
        }

        btnBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String empId = txtEmpId.getText().toString();
                String vehNum = txtVehNum.getText().toString();
                if(empId.length()!=0 || vehNum.length()!=0) {
                    Intent bookService = new Intent(getApplicationContext(),
                            BookingUpdaterService.class);
                    bookService.putExtra("slotId",txtSlotId.getText().toString());
                    bookService.putExtra("empId",empId);
                    bookService.putExtra("vehNum",vehNum);
                    bookService.setAction(BookingUpdaterService.ACTION_BOOK_SLOT);
                    startService(bookService);
                }
                else {
                    txtEmpId.setError("Either employee id or vehicle num is required");
                    txtVehNum.setError("Either employee id or vehicle num is required");
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BookingUpdaterService.BROADCAST_ACTION_BOOK_SLOT);
        registerReceiver(receiver, filter);
        Log.d("myTag", "Broadcast receiver registered");
    }

    @Override
    public void onPause() {
        unregisterReceiver(receiver);
        Log.d("myTag", "Broadcast receiver unregistered");
        super.onPause();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String response = intent.getStringExtra("response");
            if(response!=null) {
                String action = intent.getAction();
                if (action.equals(BookingUpdaterService.BROADCAST_ACTION_BOOK_SLOT)) {
                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean("error");
                        if (!error) {
                            Intent resultIntent = new Intent();
                            RegisterSlotActivity.this.setResult(1, resultIntent);
                            RegisterSlotActivity.this.finish();
                        }
                        else {
                            Toast.makeText(getApplicationContext(),
                                    jObj.getString("errorMsg"),
                                    Toast.LENGTH_SHORT).show();
                            txtEmpId.setError("Booking failed");
                            txtVehNum.setError("Booking failed");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
            else {
                Toast.makeText(getApplicationContext(),
                        "Error in connection. Either not connected to internet or wrong server addr",
                        Toast.LENGTH_LONG).show();
            }
        }
    };
}
