package com.ilp.innovations.myapplication.Fragments;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ilp.innovations.myapplication.Services.BookingUpdaterService;
import com.ilp.innovations.myapplication.Beans.Level;
import com.ilp.innovations.myapplication.MainActivity;
import com.ilp.innovations.myapplication.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class IndexFragment extends Fragment {

    private CheckBox selectAll;
    private ListView levelList;
    private ProgressDialog pDialog;
    private ArrayList<Level> levels;
    private LevelAdapter levelAdapter;

    public IndexFragment() {

    }

    public IndexFragment newInstance() {
        IndexFragment fragment = new IndexFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);
        pDialog.setTitle("Please wait");
        pDialog.setMessage("Loading floor list");
        showDialog();

        selectAll = (CheckBox) rootView.findViewById(R.id.selectAll);
        levelList = (ListView) rootView.findViewById(R.id.slotList);
        selectAll.setVisibility(View.GONE);

        levels =  new ArrayList<Level>();
        levelAdapter = new LevelAdapter(levels);
        levelList.setAdapter(levelAdapter);
        levelAdapter.notifyDataSetChanged();

        levelList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.putExtra("levelId",
                        ((TextView) view.findViewById(R.id.reg_id)).getText().toString());
                startActivity(intent);
            }
        });

        Intent levelUpdater = new Intent(getActivity(),BookingUpdaterService.class);
        levelUpdater.setAction(BookingUpdaterService.ACTION_UPDATE_LEVELS);
        getActivity().startService(levelUpdater);

        return rootView;

    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BookingUpdaterService.BROADCAST_ACTION_UPDATE_LEVELS);
        getActivity().registerReceiver(receiver, filter);
        Log.d("myTag","Broadcast receiver registered");
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(receiver);
        Log.d("myTag", "Broadcast receiver unregistered");
        super.onPause();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String response = intent.getStringExtra("response");
            hideDialog();
            if(response!=null) {
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        if (levels != null && levelAdapter != null && levelList != null) {
                            JSONArray data = jObj.getJSONArray("values");
                            for(int i=0;i<data.length();i++) {
                                JSONObject item = data.getJSONObject(i);
                                String levelId = item.getString("floorid");
                                String levelName = item.getString("floorname");
                                Level level = new Level(Integer.parseInt(levelId), levelName);
                                levels.add(level);
                            }
                            levelAdapter.notifyDataSetChanged();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
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


    private class LevelAdapter extends BaseAdapter {

        private ArrayList<Level> adpaterList = new ArrayList<Level>();

        public LevelAdapter(ArrayList<Level> adapterList) {
            this.adpaterList = adapterList;
        }


        @Override
        public int getCount() {
            return this.adpaterList.size();
        }

        @Override
        public Level getItem(int position) {
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
            Level currentLevel = getItem(position);
            String levelName = currentLevel.getLevelName();
            String allocatedLevel = "Level ID: " + String.valueOf(currentLevel.getLevelId());
            //setting the view element with corressponding value
            holder.levelName.setText(levelName);
            holder.levelNum.setText(allocatedLevel);
            holder.img.setVisibility(View.GONE);
            return convertView;
        }

        class ViewHolder {
            private TextView levelNum;
            private TextView levelName;
            private ImageView img;

            ViewHolder(View view) {
                levelNum = (TextView) view.findViewById(R.id.reg_id);
                levelName = (TextView) view.findViewById(R.id.slot);
                img = (ImageView) view.findViewById(R.id.avtar);
                view.setTag(this);
            }
        }
    }

}
