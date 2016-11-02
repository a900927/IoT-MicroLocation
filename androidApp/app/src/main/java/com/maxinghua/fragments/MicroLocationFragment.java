package com.maxinghua.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.karonl.instance.Adapter.DataAdapter;
import com.karonl.instance.InDoorView;
import com.karonl.instance.Unit.PathUnit;
import com.maxinghua.application.BackgroundService;
import com.maxinghua.errorhandle.ErrorHandler;
import com.maxinghua.main.DataJson;
import com.maxinghua.main.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


/**
 * MicroLocation Fragment that create the map based on InDoorView,
 * and display the live position for user and beacons on the map.
 * Read the message from the server, and parse the coordiant value out of the message.
 */
public class MicroLocationFragment extends Fragment {

    private TextView textview;
    private DataAdapter adapter =  new DataAdapter();;
    private Bitmap bmp;
    List<PathUnit> unitList = new ArrayList<>();

    public float lastX,lastY;
    public float unitX, unitY;
    // distance between each beacon
    private final float trangle_edge = 2;

    private TextView textView;
    private View view;
    ImageButton myBeacon1;
    ImageButton myBeacon2;
    ImageButton myBeacon3;

    private DebugFragment.OnFragmentInteractionListener mListener;
    private Handler mHandler = new Handler();

    public MicroLocationFragment() {
        // Required empty public constructor
    }

    private NewHandler handler = new NewHandler(this);
    private static class NewHandler extends Handler {
        private WeakReference<MicroLocationFragment> weakReference;
        public NewHandler(MicroLocationFragment main){
            weakReference = new WeakReference<>(main);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(weakReference.get() != null){
                float fr = (float) (msg.arg1);
                if(fr == 1000f){
                    weakReference.get().textview.setText("FPS: stop");
                } else {
                    float fps = (int)(1000f/fr);
                    weakReference.get().textview.setText("FPS: " + fps + "");
                }
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_micro_loaction, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        InDoorView view = (InDoorView) getActivity().findViewById(R.id.surface);

        myBeacon1 = (ImageButton) getActivity().findViewById(R.id.my_beacon1);
        myBeacon2 = (ImageButton) getActivity().findViewById(R.id.my_beacon2);
        myBeacon3 = (ImageButton) getActivity().findViewById(R.id.my_beacon3);
        unitX = (myBeacon3.getX() - myBeacon2.getX()) / trangle_edge;
        unitY = (myBeacon3.getY() - myBeacon1.getY()) / ((float) 1.732 * trangle_edge);

        //New thread to load the data
       Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                }catch (InterruptedException e){}

                //Background Map
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inPreferredConfig = Bitmap.Config.RGB_565;
                bmp = BitmapFactory.decodeResource(getResources(), R.drawable.map, opt);//Picture Resources
                adapter.setBmp(bmp);//Set map
                bmp = null;
                getUnitList();
                adapter.setList(unitList);//set information list
                adapter.refreshData();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getActivity().findViewById(R.id.tip).setVisibility(View.GONE);
                    }
                });
            }
        });
        ErrorHandler handle = new ErrorHandler();
        thread.setUncaughtExceptionHandler(handle);
        thread.start();

        textview = (TextView) getActivity().findViewById(R.id.frames);

        view.setAdapter(adapter);//init view

        view.setOnClickMapListener(new InDoorView.onClickMapListener() {
            @Override
            public void onClick(PathUnit region) {
                Log.e(this.getClass().getName(),"click");
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle("Store Information");
                dialog.setMessage(""+region.getName());
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                dialog.show();
            }
        });


        view.onFramesListener(new InDoorView.FramesListener() {
            @Override
            public void onRefresh(float number) {
                handler.obtainMessage(0,(int) number,0).sendToTarget();
            }
        });

        // ================ My position spot =======================
        final ImageButton myPosition = (ImageButton) getActivity().findViewById(R.id.my_postion);
        DisplayMetrics dm=getResources().getDisplayMetrics();
        final int screenWidth=dm.widthPixels;
        final int screenHeight=dm.heightPixels-50;


//        myPosition.setOnTouchListener(new View.OnTouchListener() {
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                int ea = event.getAction();
//                Log.i("TAG", "Touch:" + ea);
//
//                switch (ea) {
//                    case MotionEvent.ACTION_DOWN:
//
//                        lastX = event.getRawX();//Get raw X and Y for the motion event
//                        lastY = event.getRawY();
//                        break;
//
//                    case MotionEvent.ACTION_MOVE:
//                        float dx = event.getRawX() - lastX;
//                        float dy = event.getRawY() - lastY;
//
//                        v.setTranslationX(dx);
//                        v.setTranslationY(dy);
//
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        break;
//                }
//
//                return false;
//            }
//        });

        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (intent.hasExtra(BackgroundService.getCoordinateMessage())) {
                            lastX = myPosition.getX();
                            lastY = myPosition.getY();

                            String coor = intent.getStringExtra(BackgroundService.getCoordinateMessage());
                            // get the coordinates from the server
                            // and transfer the coordinates unit from meters to pixels
                            String[] parseString = coor.split("/");
                            float dx = Float.parseFloat(parseString[0]) * unitX - lastX;
                            float dy = Float.parseFloat(parseString[1]) * unitY - lastY;
//                            float dx = Float.parseFloat(parseString[0]) * 300 - lastX;
//                            float dy = Float.parseFloat(parseString[1]) * 300 - lastY;
                            // move the location
                            myPosition.setTranslationX(dx);
                            myPosition.setTranslationY(dy);
                            float d1 = Float.parseFloat(parseString[2]);
                            if(d1 > 1) {
                                myBeacon1.setImageResource(R.drawable.beacon2);
                            }
                            else if(d1 > 0.5) {
                                myBeacon1.setImageResource(R.drawable.beacon3);
                            }
                            else {
                                myBeacon1.setImageResource(R.drawable.beacon1);
                            }

                            float d2 = Float.parseFloat(parseString[3]);
                            if(d2 > 1) {
                                myBeacon2.setImageResource(R.drawable.beacon2);
                            }
                            else if(d2 > 0.5) {
                                myBeacon2.setImageResource(R.drawable.beacon3);
                            }
                            else {
                                myBeacon2.setImageResource(R.drawable.beacon1);
                            }

                            float d3 = Float.parseFloat(parseString[4]);
                            if(d3 > 1) {
                                myBeacon3.setImageResource(R.drawable.beacon2);
                            }
                            else if(d3 > 0.5) {
                                myBeacon3.setImageResource(R.drawable.beacon3);
                            }
                            else {
                                myBeacon3.setImageResource(R.drawable.beacon1);
                            }




                            Log.i("RECEIVE_BROADCAST", "X:" + Float.parseFloat(parseString[0]) + "Y:" + Float.parseFloat(parseString[1]));

//                            String coor = intent.getStringExtra(BackgroundService.getCoordinateMessage());
//                            String[] parseString = coor.split("/");
//
//                            if(parseString[0].equals("1")) {
//                                //move the position to 1
//
//                            } else if(parseString[0].equals("2")) {
//                                //move the position to 2
//
//                            } else if(parseString[0].equals("3")) {
//                                //move the position to 3
//
//                            } else if(parseString[0].equals("4")) {
//                                //move the position to 4
//
//                            }
                        }
                        Log.i("LOG", "X:" + "Broadcast Received");
                    }
                }, new IntentFilter(BackgroundService.getActionBroadcast())
        );

    }

    //information list
    private void getUnitList(){
        DataJson data = new DataJson();
        for(int i = 0; i < data.size(); i++){
            JSONObject jsonObject = data.getArray(i);
            PathUnit unit = new PathUnit(getList(jsonObject));
            try {
                unit.setName(jsonObject.getString("name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            unitList.add(unit);
        }
    }

    //Coordinate of list
    private List<PointF> getList(JSONObject jsonObject){
        float density = getResources().getDisplayMetrics().density;
        List<PointF> pointList = new ArrayList<>();
        JSONArray array;
        try {
            array = jsonObject.getJSONArray("area");
            for(int r = 0; r < array.length(); r++){
                float x = ((JSONObject)(array.get(r))).getInt("x");
                float y = ((JSONObject)(array.get(r))).getInt("y");
                pointList.add(new PointF( x * density, y * density));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return pointList;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}