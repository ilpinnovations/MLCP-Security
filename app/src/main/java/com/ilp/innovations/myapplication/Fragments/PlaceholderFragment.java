package com.ilp.innovations.myapplication.Fragments;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.ilp.innovations.myapplication.Services.BookingUpdaterService;
import com.ilp.innovations.myapplication.R;
import com.ilp.innovations.myapplication.Beans.Slot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.TimeZone;

public class PlaceholderFragment extends Fragment implements View.OnClickListener{


    private CheckBox selectAll;
    private ListView slotList;
    private Button btnConfirm;
    private ArrayList<Slot> confirmList;
    private ProgressDialog pDialog;
    private ArrayList<Slot> slots;
    private SlotAdapter slotAdapter;
    private AlarmManager alarmManager;
    PendingIntent pendingIntent;


    private Drawable avtar;
    private Drawable check;

    private boolean selectAllDisable = false;
    private int selectedCount;

    private int lastBookingID;

    public boolean searchBySlot;
    private SearchManager searchManager;
    private SearchView searchView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        avtar = getResources().getDrawable(R.drawable.avtar);
        check = getResources().getDrawable(R.drawable.selected_check);
    }

    public PlaceholderFragment newInstance() {
        PlaceholderFragment fragment = new PlaceholderFragment();
        return fragment;
    }

    public PlaceholderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        selectAll = (CheckBox) rootView.findViewById(R.id.selectAll);
        btnConfirm = (Button) rootView.findViewById(R.id.confirmAll);
        slotList = (ListView) rootView.findViewById(R.id.slotList);

        slots =  new ArrayList<Slot>();
        slotAdapter = new SlotAdapter(slots);
        slotList.setAdapter(slotAdapter);
        slotAdapter.notifyDataSetChanged();

        confirmList = new ArrayList<Slot>();
        btnConfirm.setOnClickListener(this);
        pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);
        pDialog.setTitle("Please wait");
        pDialog.setMessage("Loading allocated slot list");
        showDialog();

        slotList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectAllDisable = true;
                ImageView img = (ImageView) view.findViewById(R.id.avtar);
                Slot slot = slots.get(position);
                if (slot.isChecked()) {
                    selectedCount--;
                    confirmList.remove(slots.get(position));
                    slot.setIsChecked(false);
                    img.setImageDrawable(avtar);
                } else {
                    selectedCount++;
                    confirmList.add(slots.get(position));
                    slot.setIsChecked(true);
                    img.setImageDrawable(check);
                }
                if (selectedCount == slots.size())
                    selectAll.setChecked(true);
                else
                    selectAll.setChecked(false);

                if (selectedCount > 0)
                    btnConfirm.setVisibility(View.VISIBLE);
                else
                    btnConfirm.setVisibility(View.GONE);
                selectAllDisable = false;
            }
        });

        slotList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, final int position, long id) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                final EditText txtNewSlot = new EditText(getActivity());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                txtNewSlot.setLayoutParams(lp);
                txtNewSlot.setHint(getResources().getString(R.string.txt_new_slot_num));
                builder.setTitle("CHANGE VEHICLE");
                builder.setMessage("Currently Selected Slot : " + slots.get(position).getSlot() +
                        "\nCurrently Allocated Vehicle : " + slots.get(position).getRegId() + "\n\n");
                builder.setView(txtNewSlot);
                builder.setPositiveButton("CHANGE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String regId = txtNewSlot.getText().toString();
                        Slot slot = slots.get(position);
                        slot.setRegId(regId);
                        Intent vehicleChange = new Intent(getActivity(),BookingUpdaterService.class);
                        vehicleChange.setAction(BookingUpdaterService.ACTION_CHANGE_SLOT);
                        vehicleChange.putExtra("bookingId", String.valueOf(slot.getBookId()));
                        vehicleChange.putExtra("regNo",regId);
                        getActivity().startService(vehicleChange);
                        slotAdapter.notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                //builder.setView(inflater.inflate(R.layout.activity_dialogue, null));

                AlertDialog confirmAlert = builder.create();
                confirmAlert.show();
                return true;
            }
        });

        selectAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!selectAllDisable) {

                    slotAdapter = new SlotAdapter(slots);
                    slotList.setAdapter(slotAdapter);
                    btnConfirm.setVisibility(View.VISIBLE);
                    selectedCount = slots.size();
                    if (!isChecked) {
                        selectedCount = 0;
                        btnConfirm.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(getActivity(), "Selected count = " + slots.size(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(Slot eachSlot:confirmList) {
                    Intent confirmSlot = new Intent(getActivity(),BookingUpdaterService.class);
                    confirmSlot.setAction(BookingUpdaterService.ACTION_CONFIRM_PARKING);
                    confirmSlot.putExtra("bookingId", String.valueOf(eachSlot.getBookId()));
                    getActivity().startService(confirmSlot);
                }
                slots.clear();
                confirmList.clear();
                lastBookingID = 0;
                slotAdapter.notifyDataSetChanged();
                //reloading list after confirmation of all the slots
                Intent intent = new Intent(getActivity(),BookingUpdaterService.class);
                intent.setAction(BookingUpdaterService.ACTION_SLOT_ALLOCATION);
                intent.putExtra("bookingId",String.valueOf(lastBookingID));
                getActivity().startService(intent);
            }
        });

        /*setAlarmToUpdateSlots(getActivity());*/

        Intent intent = new Intent(getActivity(),BookingUpdaterService.class);
        intent.setAction(BookingUpdaterService.ACTION_SLOT_ALLOCATION);
        intent.putExtra("bookingId",String.valueOf(lastBookingID));
        getActivity().startService(intent);

        return rootView;
    }



    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BookingUpdaterService.BROADCAST_ACTION_CONFIRM_PARKING);
        filter.addAction(BookingUpdaterService.BROADCAST_ACTION_CLEAR_SLOT);
        filter.addAction(BookingUpdaterService.BROADCAST_ACTION_CHANGE_SLOT);
        filter.addAction(BookingUpdaterService.BROADCAST_ACTION_SLOT_ALLOCATION);
        getActivity().registerReceiver(receiver, filter);
        Log.d("myTag", "Broadcast receiver registered");
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(receiver);
        if(alarmManager!=null)
            alarmManager.cancel(pendingIntent);
        Log.d("myTag", "Broadcast receiver unregistered");
        super.onPause();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String response = intent.getStringExtra("response");
            hideDialog();
            if(response!=null) {
                String action = intent.getAction();
                if (action.equals(BookingUpdaterService.BROADCAST_ACTION_SLOT_ALLOCATION)) {
                    try {
                        Log.d("myTag", response);
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean("error");
                        // Check for error node in json
                        if (!error) {
                            JSONArray data = jObj.getJSONArray("values");
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject item = data.getJSONObject(i);
                                Slot slot = new Slot();
                                slot.setBookId(Integer.parseInt(item.getString("booking_id")));
                                slot.setEmpId(item.getString("employeeid"));
                                slot.setRegId(item.getString("vehiclenumber"));
                                slot.setSlot(item.getString("slotid"));
                                slots.add(slot);
                                lastBookingID = slot.getBookId();
                            }
                            slotAdapter.notifyDataSetChanged();
                            hideDialog();
                        } else {
                            // Error in login. Get the error message
                            String errorMsg = jObj.getString("errorMsg");
                            Toast.makeText(getActivity(),
                                    errorMsg, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException je) {
                        je.printStackTrace();
                        Toast.makeText(getActivity(),
                                "Error in response!",
                                Toast.LENGTH_SHORT).show();
                    } catch (NullPointerException ne) {
                        Toast.makeText(getActivity(),
                                "Error in connection! Please check your connection",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                else if(action.equals(BookingUpdaterService.BROADCAST_ACTION_CONFIRM_PARKING) ||
                        action.equals(BookingUpdaterService.BROADCAST_ACTION_CHANGE_SLOT)) {
                    try {
                        Log.d("myTag", response);
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean("error");
                        // Check for error node in json
                        if (!error) {
                            //success confirming slot
                        }
                        else {
                            Toast.makeText(getActivity(),
                                    jObj.getString("errorMsg"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(),
                                "Error in response!",
                                Toast.LENGTH_SHORT).show();
                    } catch (NullPointerException ne) {
                        Toast.makeText(getActivity(),
                                "Error in connection! Please check your connection",
                                Toast.LENGTH_SHORT).show();
                    }

                }
            }
            else {
                Toast.makeText(getActivity(),
                        "Error in connection. Please verify your internet connection and server adsress",
                        Toast.LENGTH_LONG).show();
            }

        }
    };


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // Inflate the menu items for use in the action bar
        inflater.inflate(R.menu.menu_main, menu);

        if(searchBySlot) {
            menu.findItem(R.id.search_slot).setIcon(getResources().getDrawable(R.drawable.check));
            menu.findItem(R.id.search_reg_num).setIcon(null);
        }
        else {
            menu.findItem(R.id.search_reg_num).setIcon(getResources().getDrawable(R.drawable.check));
            menu.findItem(R.id.search_slot).setIcon(null);
        }

        searchManager =
                (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setBackgroundColor(getResources().getColor(R.color.bg_search));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText==null || newText.length()==0)
                    return false;
                if (slots != null) {
                    ArrayList<Slot> updatedSlots = new ArrayList<Slot>();
                    for (Slot eachSlot : slots) {
                        if (searchBySlot && eachSlot.getSlot().contains(newText)) {
                            updatedSlots.add(eachSlot);
                        } else if (!searchBySlot && eachSlot.getRegId()!=null) {
                            if(eachSlot.getRegId().contains(newText))
                                updatedSlots.add(eachSlot);
                        }
                    }
                    slotAdapter = new SlotAdapter(updatedSlots);
                    slotList.setAdapter(slotAdapter);
                }
                return false;
            }
        });
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
                Intent updateSlots = new Intent(getActivity(),BookingUpdaterService.class);
                updateSlots.setAction(BookingUpdaterService.ACTION_SLOT_ALLOCATION);
                updateSlots.putExtra("bookingId", String.valueOf(lastBookingID));
                getActivity().startService(updateSlots);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onClick(View v) {

    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

/*    public void setAlarmToUpdateSlots(Context context) {
        final long REPEAT_TIME = 1000*3;

        Calendar updateTime =  Calendar.getInstance();
        updateTime.setTimeZone(TimeZone.getDefault());
        updateTime.setTimeInMillis(System.currentTimeMillis());

        Intent updateSlots = new Intent(context,BookingUpdaterService.class);
        updateSlots.setAction(BookingUpdaterService.ACTION_SLOT_ALLOCATION);
        updateSlots.putExtra("bookingId", String.valueOf(lastBookingID));

        pendingIntent = PendingIntent.getService(context,0,
                updateSlots,PendingIntent.FLAG_CANCEL_CURRENT);

        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,updateTime.getTimeInMillis(),
                REPEAT_TIME,pendingIntent);
        Log.d("myTag","Alarm set");

    }*/

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
            Slot currentSlot = getItem(position);
            String titleString = currentSlot.getRegId();
            String allocatedSlot = "Slot: " + currentSlot.getSlot();
            //setting the view element with corressponding value
            holder.regId.setText(titleString);
            holder.slot.setText(allocatedSlot);
            holder.img.setImageDrawable(avtar);
            if (selectAll.isChecked()) {
                holder.img.setImageDrawable(check);
                confirmList.add(slots.get(position));
            }
            return convertView;
        }

        class ViewHolder {
            private TextView regId;
            private TextView slot;
            private ImageView img;

            ViewHolder(View view) {
                regId = (TextView) view.findViewById(R.id.reg_id);
                slot = (TextView) view.findViewById(R.id.slot);
                img = (ImageView) view.findViewById(R.id.avtar);
                view.setTag(this);
            }
        }
    }


}
