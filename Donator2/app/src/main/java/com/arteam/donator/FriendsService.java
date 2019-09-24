package com.arteam.donator;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.UUID;

public class FriendsService extends Service {

    public static final UUID appUUID = UUID.fromString("306cc03f-27ab-4ef8-8573-c551c2a56c53");
    public static final String domainName = "Donator";

    private BluetoothAdapter mAdapter;
    private Context ctx;

    private ConnectedThread mConnectedThread;
    private AcceptThread mAcceptThread;

    public void start(){
        if(mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if(mAcceptThread == null){ //osluskuje konekcije
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
    }

    private class AcceptThread extends Thread
    {
        private BluetoothServerSocket threadBts = null;

        public AcceptThread()
        {
            IOException e = null;
            try {
                Log.i("FriendsService", "AcceptThread constructor");
                BluetoothServerSocket tmp = mAdapter.listenUsingRfcommWithServiceRecord(domainName, appUUID);
                threadBts = tmp;
            } catch(IOException ex){
                e = ex;
            }
        }

        public void run()
        {
            BluetoothSocket socket = null;
            Log.i("FriendsService", "AcceptThread run");
            while(true) {
                try {
                    socket = threadBts.accept();
                } catch (IOException ex) {
                    Log.e("FriendsService", "Socket's accept() method failed", ex);
                    break;
                }

                if (socket != null) {

                    mConnectedThread = new ConnectedThread(socket);
                    mConnectedThread.start();
                    try {
                        threadBts.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        public void cancel(){
            try{
                threadBts.close();
            } catch(IOException ex){

            }
        }
    }


    private class ConnectedThread extends Thread
    {
        private BluetoothSocket mBts;
        private InputStream is;

        public ConnectedThread(BluetoothSocket socket){
            Log.i("FriendsService", "ConnectedThread constructor");
            mBts = socket;
            try{
                is = mBts.getInputStream();
            } catch(IOException ex){
            }
        }

        @Override
        public void run(){
            Log.i("FriendsService", "ConnectedThread run");
            try {
                while(is.available() == 0) try{
                    sleep(500);
                }catch(Exception ex){

                }
                if (is.available() > 0) {
                    ObjectInputStream in = new ObjectInputStream(is);
                    try {
                        UserRequest user = (UserRequest) in.readObject();
                        Intent i = new Intent("friends.filter");
                        i.putExtra("userID", user.ID);
                        i.putExtra("first_name", user.first_name);
                        i.putExtra("last_name", user.last_name);
                        ctx.sendBroadcast(i);
                    } catch (ClassNotFoundException e) {
                    }
                }
            } catch(IOException ex){
                ex.printStackTrace();
            }
            try{
                mBts.close();
                mAcceptThread = new AcceptThread();
                mAcceptThread.run();
            }catch(IOException ex){}
        }


        public void cancel(){
            try{
                mBts.close();
            }catch(IOException ex){}
        }


    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public FriendsService() {
    }


    @Override
    public void onCreate()
    {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        ctx = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {

        start();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy()
    {
        stopSelf();
        super.onDestroy();
    }

}
