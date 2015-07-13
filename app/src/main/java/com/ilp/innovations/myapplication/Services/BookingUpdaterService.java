package com.ilp.innovations.myapplication.Services;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import com.github.kevinsawicki.http.HttpRequest;
import java.util.HashMap;
import java.util.Map;

public class BookingUpdaterService extends IntentService {

    public static final String BROADCAST_ACTION_UPDATE_LEVELS="com.ilp.innovations.UPDATE_LEVELS";
    public static final String BROADCAST_ACTION_SLOT_ALLOCATION="com.ilp.innovations.SLOT_ALLOCATION";
    public static final String BROADCAST_ACTION_CONFIRM_PARKING="com.ilp.innovations.CONFIRM_PARKING";
    public static final String BROADCAST_ACTION_CLEAR_SLOT="com.ilp.innovations.CLEAR_SLOT";
    public static final String BROADCAST_ACTION_CHANGE_SLOT="com.ilp.innovations.CHANGE_SLOT";
    public static final String BROADCAST_ACTION_GET_ALL_SLOTS="com.ilp.innovations.GET_ALL_SLOTS";
    public static final String BROADCAST_ACTION_GET_RESERVED_SLOTS="com.ilp.innovations.GET_RESERVED_SLOTS";
    public static final String BROADCAST_ACTION_GET_CONFIRMED_SLOTS="com.ilp.innovations.GET_CONFIRMED_SLOTS";
    public static final String BROADCAST_ACTION_BOOK_SLOT="com.ilp.innovations.BOOK_SLOT";
    public static final String BROADCAST_ACTION_RESERVE_SLOT="com.ilp.innovations.RESERVE_SLOT";
    public static final String BROADCAST_ACTION_SWIPE_OUT="com.ilp.innovations.SWIPE_OUT";

    public static final String ACTION_UPDATE_LEVELS="GetFloors";
    public static final String ACTION_SLOT_ALLOCATION="GetBookingDetails";
    public static final String ACTION_CONFIRM_PARKING="ConfirmParking";
    public static final String ACTION_CLEAR_SLOT="ClearSlot";
    public static final String ACTION_CHANGE_SLOT="ChangeSlot";
    public static final String ACTION_GET_ALL_SLOTS="GetAllSlots";
    public static final String ACTION_GET_RESERVED_SLOTS ="GetReservedSlots";
    public static final String ACTION_BOOK_SLOT="BookSlot";
    public static final String ACTION_GET_CONFIRMED_SLOTS="GetConfirmedSlots";
    public static final String ACTION_RESERVE_SLOT="ReserveSlot";
    public static final String ACTION_SWIPE_OUT="SwipeOut";

    private static final String SERVER_ADDR = "192.168.1.100";
    private String BROADCAST_ACTION;

    public BookingUpdaterService() {
        super("BookingUpdaterService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("myTag","Service started");
        if (intent != null) {
            final String action = intent.getAction();
            Map<String,String> params = new HashMap<>();
            switch (action) {
                case ACTION_UPDATE_LEVELS:
                    params.put("tag", ACTION_UPDATE_LEVELS);
                    BROADCAST_ACTION = BROADCAST_ACTION_UPDATE_LEVELS;
                    break;
                case ACTION_SLOT_ALLOCATION:
                    String lastBookingId = intent.getStringExtra("bookingId");
                    params.put("tag", ACTION_SLOT_ALLOCATION);
                    params.put("bookingId", lastBookingId);
                    BROADCAST_ACTION = BROADCAST_ACTION_SLOT_ALLOCATION;
                    break;
                case ACTION_CHANGE_SLOT: {
                    String bookingId = intent.getStringExtra("bookingId");
                    String vehicleNum = intent.getStringExtra("regNo");
                    params.put("tag", ACTION_CHANGE_SLOT);
                    params.put("bookingId", bookingId);
                    params.put("newVehicleNumber", vehicleNum);
                    BROADCAST_ACTION = BROADCAST_ACTION_CHANGE_SLOT;
                    break;
                }
                case ACTION_CONFIRM_PARKING: {
                    String bookingId = intent.getStringExtra("bookingId");
                    params.put("tag", ACTION_CONFIRM_PARKING);
                    params.put("bookingId", bookingId);
                    BROADCAST_ACTION = BROADCAST_ACTION_CONFIRM_PARKING;
                    break;
                }
                case ACTION_CLEAR_SLOT:
                    String slotIdToClear = intent.getStringExtra("slotId");
                    params.put("tag", ACTION_CLEAR_SLOT);
                    params.put("slotId", slotIdToClear);
                    BROADCAST_ACTION = BROADCAST_ACTION_CLEAR_SLOT;
                    break;
                case ACTION_RESERVE_SLOT:
                    String slotIdToReserve = intent.getStringExtra("slotId");
                    params.put("tag", ACTION_RESERVE_SLOT);
                    params.put("slotId", slotIdToReserve);
                    BROADCAST_ACTION = BROADCAST_ACTION_RESERVE_SLOT;
                    break;
                case ACTION_GET_ALL_SLOTS:
                    params.put("tag", ACTION_GET_ALL_SLOTS);
                    BROADCAST_ACTION = BROADCAST_ACTION_GET_ALL_SLOTS;
                    break;
                case ACTION_GET_RESERVED_SLOTS:
                    params.put("tag", ACTION_GET_RESERVED_SLOTS);
                    BROADCAST_ACTION = BROADCAST_ACTION_GET_RESERVED_SLOTS;
                    break;
                case ACTION_GET_CONFIRMED_SLOTS:
                    params.put("tag", ACTION_GET_CONFIRMED_SLOTS);
                    BROADCAST_ACTION = BROADCAST_ACTION_GET_CONFIRMED_SLOTS;
                    break;
                case ACTION_BOOK_SLOT:
                    String vehNum = intent.getStringExtra("vehNum");
                    String empId = intent.getStringExtra("empId");
                    String slotId = intent.getStringExtra("slotId");
                    params.put("tag", ACTION_BOOK_SLOT);
                    params.put("employeeId", empId);
                    params.put("vehicleNumber", vehNum);
                    params.put("slotId",slotId);
                    BROADCAST_ACTION = BROADCAST_ACTION_BOOK_SLOT;
                    break;
                case ACTION_SWIPE_OUT:
                    String bookingId = intent.getStringExtra("bookingId");
                    params.put("tag",ACTION_SWIPE_OUT);
                    params.put("bookingId",bookingId);
                    BROADCAST_ACTION = BROADCAST_ACTION_SWIPE_OUT;
                    break;
            }
            params.put("bcast_action",BROADCAST_ACTION);
            new HttpRequestTask(params).execute(SERVER_ADDR);
        }
    }

    private class HttpRequestTask extends AsyncTask<String, String, String> {

        private Map<String,String> params;

        public HttpRequestTask(Map<String,String> data) {
            this.params = data;
        }

        protected String doInBackground(String... urls) {

            String response=null;
            try {
                String url = "http://" + urls[0] + "/mlcp_new/index.php";
                Log.d("myTag", url);
                response = HttpRequest.post(url)
                        .accept("application/json")
                        .form(params)
                        .body();
                Log.d("myTag","Response-->"+response);

            } catch (HttpRequest.HttpRequestException exception) {
                exception.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return  response;
        }


        protected void onPostExecute(String response) {
            Intent notification = new Intent();
            Log.d("myTag",params.get("bcast_action"));
            notification.setAction(params.get("bcast_action"));
            /*
            *The below commented code is reqd when the intent has to start an activity which
            *is already in stopped state
            */
            //notification.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            notification.putExtra("response",response);
            sendBroadcast(notification);
            Log.d("myBCast","action:"+params.get("bcast_action")+"\nresp:"+response);
        }
    }

}
