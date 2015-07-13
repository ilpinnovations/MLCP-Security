package com.ilp.innovations.myapplication.Fragments;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.ilp.innovations.myapplication.Beans.Slot;
import com.ilp.innovations.myapplication.R;
import com.ilp.innovations.myapplication.RegisterSlotActivity;
import com.ilp.innovations.myapplication.Services.BookingUpdaterService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class ReservedSlotsFragment extends Fragment {

    private ProgressDialog pDialog;
    private ListView slotList;
    private ArrayList<Slot> slots;
    private SlotAdapter slotAdapter;
    public boolean searchBySlot;
    private SearchManager searchManager;
    private SearchView searchView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public ReservedSlotsFragment() {
    }

    public Fragment newInstance() {
        ReservedSlotsFragment fragment = new ReservedSlotsFragment();
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);
        pDialog.setTitle("Please wait");
        pDialog.setMessage("Loading slots list");
        showDialog();

        slotList = (ListView) rootView.findViewById(R.id.slotList);
        slots = new ArrayList<>();
        slotAdapter = new SlotAdapter(slots);
        slotList.setAdapter(slotAdapter);

        Intent intent = new Intent(getActivity(),BookingUpdaterService.class);
        intent.setAction(BookingUpdaterService.ACTION_GET_RESERVED_SLOTS);
        getActivity().startService(intent);

        slotList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Manage Reserved Slot");
                builder.setMessage("What do you want to do?");
                final Slot selectedSlot = slots.get(position);
                builder.setPositiveButton("BOOK VEHICLE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent bookIntent = new Intent(getActivity(), RegisterSlotActivity.class);
                        bookIntent.putExtra("slotId",String.valueOf(selectedSlot.getSlotId()));
                        startActivityForResult(bookIntent, 1);
                    }
                });
                builder.setNegativeButton("RELEASE SLOT", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //todo release the slot here
                        showDialog();
                        Intent releaseIntent = new Intent(getActivity(),BookingUpdaterService.class);
                        releaseIntent.setAction(BookingUpdaterService.ACTION_CLEAR_SLOT);
                        releaseIntent.putExtra("slotId",selectedSlot.getSlotId());
                        getActivity().startService(releaseIntent);
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BookingUpdaterService.BROADCAST_ACTION_GET_RESERVED_SLOTS);
        filter.addAction(BookingUpdaterService.BROADCAST_ACTION_CLEAR_SLOT);
        getActivity().registerReceiver(receiver, filter);
        Log.d("myTag", "Broadcast receiver registered");
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(receiver);
        Log.d("myTag", "Broadcast receiver unregistered");
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case 1:
                //refresh dataset after successful booking
                Toast.makeText(getActivity(),"Booking successful!",
                        Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(),BookingUpdaterService.class);
                intent.setAction(BookingUpdaterService.ACTION_GET_RESERVED_SLOTS);
                getActivity().startService(intent);
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // Inflate the menu items for use in the action bar
        inflater.inflate(R.menu.menu_main, menu);

        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.search_head).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.search_slot:
                searchBySlot = true;
                getActivity().invalidateOptionsMenu();
                break;
            case R.id.search_reg_num:
                searchBySlot = false;
                getActivity().invalidateOptionsMenu();
                break;
            case R.id.action_refresh:
                showDialog();
                Intent intent = new Intent(getActivity(),BookingUpdaterService.class);
                intent.setAction(BookingUpdaterService.ACTION_GET_RESERVED_SLOTS);
                getActivity().startService(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String response = intent.getStringExtra("response");
            hideDialog();
            if(response!=null) {
                String action = intent.getAction();
                if (action.equals(BookingUpdaterService.BROADCAST_ACTION_GET_RESERVED_SLOTS)) {
                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean("error");
                        if (!error) {
                            slots.clear();
                            slotAdapter = new SlotAdapter(slots);
                            slotList.setAdapter(slotAdapter);
                            JSONArray data = jObj.getJSONArray("values");
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject item = data.getJSONObject(i);
                                Slot slot = new Slot();
                                String slotId = item.getString("slotid");
                                slot.setSlotId(slotId);
                                slot.setSlot(item.getString("slotname"));
                                slots.add(slot);
                            }
                            slotAdapter.notifyDataSetChanged();
                            hideDialog();
                        }
                        else {
                            Toast.makeText(getActivity(),jObj.getString("errorMsg"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else if(action.equals(BookingUpdaterService.BROADCAST_ACTION_CLEAR_SLOT)) {
                    hideDialog();
                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean("error");
                        if (!error) {
                            slots.clear();
                            slotAdapter.notifyDataSetChanged();
                            showDialog();
                            Intent updateIntent = new Intent(getActivity(), BookingUpdaterService.class);
                            updateIntent.setAction(BookingUpdaterService.ACTION_GET_RESERVED_SLOTS);
                            getActivity().startService(updateIntent);
                        }
                        else {
                            Toast.makeText(getActivity(),jObj.getString("errorMsg"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
            else {
                Toast.makeText(getActivity(),
                        "Error in connection. Either not connected to internet or wrong server addr",
                        Toast.LENGTH_LONG).show();
            }
        }
    };

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private class SlotAdapter extends BaseAdapter {

        private ArrayList<Slot> adpaterList = new ArrayList<Slot>();

        public SlotAdapter(ArrayList<Slot> adapterList) {
            this.adpaterList = adapterList;
        }


        @Override
        public int getCount() {
            return this.adpaterList.size();
        }

        @Override
        public Slot getItem(int position) {
            return this.adpaterList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                //setting the view with list item
                convertView = View.inflate(getActivity(), R.layout.slot_item, null);

                // This class is necessary to identify the list item, in case convertView!=null
                new ViewHolder(convertView);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            //getting view elements value from ArrayList

            Slot slot = getItem(position);
            holder.slotId.setText("Slot ID:"+String.valueOf(slot.getSlotId()));
            holder.slotName.setText(slot.getSlot());
            holder.img.setVisibility(View.GONE);

            return convertView;
        }

        class ViewHolder {
            private TextView slotId;
            private TextView slotName;
            private ImageView img;

            ViewHolder(View view) {
                slotName = (TextView) view.findViewById(R.id.reg_id);
                slotId = (TextView) view.findViewById(R.id.slot);
                img = (ImageView) view.findViewById(R.id.avtar);
                view.setTag(this);
            }
        }
    }

}
