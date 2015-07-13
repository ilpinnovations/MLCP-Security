package com.ilp.innovations.myapplication.Fragments;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.kevinsawicki.http.HttpRequest;
import com.ilp.innovations.myapplication.R;
import com.ilp.innovations.myapplication.Beans.Slot;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

public class UnAllocatedSlotsFragment extends Fragment implements View.OnClickListener{

    private final String ARG_SECTION_NUMBER = "section_number";

    private CheckBox selectAll;
    private ListView slotList;
    private ProgressDialog pDialog;
    private ArrayList<Slot> slots;
    private SlotAdapter slotAdapter;

    private Drawable question;

    public boolean searchBySlot;
    private SearchManager searchManager;
    private SearchView searchView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        question = getResources().getDrawable(R.drawable.questionmark);
    }

    public UnAllocatedSlotsFragment newInstance() {
        UnAllocatedSlotsFragment fragment = new UnAllocatedSlotsFragment();
        return fragment;
    }

    public UnAllocatedSlotsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        selectAll = (CheckBox) rootView.findViewById(R.id.selectAll);
        slotList = (ListView) rootView.findViewById(R.id.slotList);

        selectAll.setVisibility(View.GONE);
        slots =  new ArrayList<Slot>();
        slotAdapter = new SlotAdapter(slots);
        slotList.setAdapter(slotAdapter);
        Slot s1 = new Slot(1,"","A1");
        Slot s2 = new Slot(2,"","A2");
        Slot s3 = new Slot(3,"","A3");
        slots.add(s1);
        slots.add(s2);
        slots.add(s3);
        slotAdapter.notifyDataSetChanged();

        pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);

        slotList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                final EditText txtNewSlot = new EditText(getActivity());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                txtNewSlot.setLayoutParams(lp);
                txtNewSlot.setHint(getResources().getString(R.string.txt_new_slot_num));
                builder.setTitle("CHANGE VEHICLE");
                builder.setMessage("Selected Slot : " +slots.get(position).getSlot()+"\n\n");
                builder.setView(txtNewSlot);
                builder.setPositiveButton("CHANGE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String regId = txtNewSlot.getText().toString();
                        slots.get(position).setRegId(regId);
                        slotAdapter.notifyDataSetChanged();
                        //do something to update slot vehicle
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
            }
        });


        return rootView;

    }

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
                if(slots!=null) {
                    ArrayList<Slot> updatedSlots = new ArrayList<Slot>();
                    for(Slot eachSlot:slots) {
                        if(searchBySlot && eachSlot.getSlot().contains(newText))
                        {
                            updatedSlots.add(eachSlot);
                        }
                        else if(!searchBySlot && eachSlot.getRegId().contains(newText)) {
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

    private class HttpRequestTask extends AsyncTask<String, String, String> {

        private Map<String,String> params;

        public HttpRequestTask(Map<String,String> data) {
            this.params = data;
        }

        protected String doInBackground(String... urls) {

            String response=null;
            try {
                String url = "http://" + urls[0] + "/index.php";
                Log.d("myTag", url);
                response = HttpRequest.post(url)
                        .accept("application/json")
                        .form(params)
                        .body();
                Log.d("myTag","Response-->"+response);

            } catch (HttpRequest.HttpRequestException exception) {
                exception.printStackTrace();
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return  response;
        }


        protected void onPostExecute(String response) {
            try {
                Log.d("myTag", response);
                JSONObject jObj = new JSONObject(response);
                boolean error = jObj.getBoolean("error");

                // Check for error node in json
                if (!error) {

                } else {
                    // Error in login. Get the error message
                    String errorMsg = jObj.getString("error_msg");
                    Toast.makeText(getActivity(),
                            errorMsg, Toast.LENGTH_LONG).show();
                }
            }  catch(JSONException je) {
                je.printStackTrace();
                Toast.makeText(getActivity(),
                        "Error in response!",
                        Toast.LENGTH_SHORT).show();
            } catch (NullPointerException ne) {
                Toast.makeText(getActivity(),
                        "Error in connection! Please check your connection",
                        Toast.LENGTH_SHORT).show();
            } finally {
                hideDialog();
            }

        }
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
            Slot currentSlot = getItem(position);
            String titleString = currentSlot.getRegId();
            String allocatedSlot = "Slot: " + currentSlot.getSlot();
            //setting the view element with corressponding value
            holder.regId.setText(titleString);
            holder.slot.setText(allocatedSlot);
            holder.img.setImageDrawable(question);
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
