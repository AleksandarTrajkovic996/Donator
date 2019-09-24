package com.arteam.donator;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PickDevices extends AppCompatActivity {

    Context c = this;
    BluetoothAdapter mBluetoothAdapter;
    private ArrayList<String> mDeviceList = new ArrayList<String>();
    private ListView listView;
    private TextView fullNameNavigationMain;
    BluetoothSocket mSocket;
    private String userFirstName;
    private String userLastName;
    ArrayAdapter<String> arrayAdapter;
    public HashMap<String, String> mAddressMapping = new HashMap<String,String>();


    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver= new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(!mAddressMapping.containsKey(device.getName())) {
                    mDeviceList.add(device.getName());
                    mAddressMapping.put(device.getName(), device.getAddress());
                    listView.invalidateViews();
                    arrayAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

        setContentView(R.layout.activity_pick_devices);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        listView = (ListView) findViewById(R.id.listViewDevices);

        Intent i = getIntent();
        userLastName = i.getStringExtra("last");
        userFirstName = i.getStringExtra("first");
        //registerReceiver(receiver, filter);

        arrayAdapter =  new ArrayAdapter(c, android.R.layout.simple_list_item_1, mDeviceList);

        if(mBluetoothAdapter!=null){
            setupListener();
        }

    }

    @Override
    protected void onDestroy() {
        mBluetoothAdapter.cancelDiscovery();
        mBluetoothAdapter.disable();
        mAddressMapping.clear();
        mDeviceList.clear();
        //unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        mBluetoothAdapter.cancelDiscovery();
        mBluetoothAdapter.disable();
        mAddressMapping.clear();
        mDeviceList.clear();
        //unregisterReceiver(receiver);
        super.onStop();
    }


    protected void setupListener(){
        if(mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {

            if(mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
                mAddressMapping.clear();
                mDeviceList.clear();
            }

            mBluetoothAdapter.startDiscovery();
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            mBluetoothAdapter.cancelDiscovery();


            for(BluetoothDevice device: pairedDevices)
            {
                if(device.getName() != null && !mAddressMapping.containsKey(device.getName())) {
                    mDeviceList.add(device.getName());
                    mAddressMapping.put(device.getName(), device.getAddress());
                    listView.setAdapter(arrayAdapter);
                    arrayAdapter.notifyDataSetChanged();
                }
            }


            ListView lw = findViewById(R.id.listViewDevices);
            lw.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final AdapterView<?> parentPom = parent;
                    final int positionPom = position;

                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            String item =(String) parentPom.getItemAtPosition(positionPom);
                            String address = mAddressMapping.get(item);
                            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                            try {
                                mSocket = device.createRfcommSocketToServiceRecord(FriendsService.appUUID);
                                mSocket.connect();
                                OutputStream os = mSocket.getOutputStream();

                                UserRequest user = new UserRequest();
                                String userID = FirebaseSingleton.getInstance().mAuth.getCurrentUser().getUid();
                                user.ID = userID;
                                user.first_name = userFirstName;
                                user.last_name = userLastName;

                                ObjectOutputStream o = new ObjectOutputStream(os);
                                o.writeObject(user);
                            }catch(IOException e){
                                e.printStackTrace();
                            }
                            catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
        }
    }


}
