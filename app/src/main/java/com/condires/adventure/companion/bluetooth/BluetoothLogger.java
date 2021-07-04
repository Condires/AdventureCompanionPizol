package com.condires.adventure.companion.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.condires.adventure.companion.Data;
import com.condires.adventure.companion.MainActivity;
import com.condires.adventure.companion.R;
import com.condires.adventure.companion.alarm.Alarm;
import com.condires.adventure.companion.audio.CompanionAudioService;
import com.condires.adventure.companion.logger.LogService;
import com.condires.adventure.companion.model.Anlage;
import com.condires.adventure.companion.setting.ACSettings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BluetoothLogger {

    private String TAG = this.getClass().getSimpleName();
    public static String EXTRA_DEVICE_ADDRESS = "device_address";  // * Return Intent extra
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private BluetoothAdapter     mBluetoothAdapter = null;  // Local Bluetooth adapter
    private BluetoothChatService mChatService = null;  // Member object for the chat services

    private StringBuilder        chatLogMessage = new StringBuilder();  // nur die Meldungen
    private StringBuffer         mOutStringBuffer; // String buffer for outgoing messages

    CompanionAudioService mCompanionAudioService;
    AppCompatActivity   mActivity;
    Data                mData;


    // Eine (versteckte) Klassenvariable vom Typ der eigenen Klasse
    private static BluetoothLogger instance;

    public void setActivity(AppCompatActivity mActivity) {
        this.mActivity = mActivity;
    }


    public static BluetoothLogger getInstance (AppCompatActivity activity) {
        if (BluetoothLogger.instance == null) {
            BluetoothLogger.instance = new BluetoothLogger (activity);

        }
        return BluetoothLogger.instance;
    }

    // Verhindere die Erzeugung des Objektes über andere Methoden
    private BluetoothLogger (AppCompatActivity activity) {
        if (instance == null) {
            instance = this;
        }
        // hier wird die Instanz erzeugt
        mCompanionAudioService = CompanionAudioService.getInstance(activity);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mActivity = activity;
        mData = Data.getInstance();
        setupBT();
    }

    // Eine Zugriffsmethode auf Klassenebene, welches dir '''einmal''' ein konkretes
    // Objekt erzeugt und dieses zurückliefert.
    private Data data() {
        return Data.getInstance();
    }


    // user um Erlaubnis fragen, Bluetooth zu benutzen
    public void setupBT() {
        // If BT is not on, request that it be enabled.
        setupChat(); //will then be called during onActivityResult
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                mActivity.startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                // Otherwise, setup the chat session
            }
        }
    }


    private void setupChat() {
        com.condires.adventure.companion.logwrapper.Log.d(TAG, "setupChat() (via LogWrapper)");
        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(MainActivity.getContext(), mHandler);
        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    public String getDeviceName() {
        // Get local Bluetooth adapter
        //mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        String deviceName = "unbekannt";
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            deviceName = android.os.Build.DEVICE;
            Toast.makeText(MainActivity.getContext(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
        } else {
            deviceName = mBluetoothAdapter.getName();
            //String newName = "Wangs-t1";
            //if (mDeviceName.equalsIgnoreCase(newName) == false) {
            //    BluetoothChatService.renameBluetoothDeviceName(newName);

            //}
        }
        return deviceName;
    }

    public void writeBluetoothLog(String msg) {
        //TODO nicht nach newline, sonden nach <Log> suchen
        chatLogMessage.append(msg + "\n");
        // TODO zwei Runden halten und dann jeweils eine Runde löschen
        if (chatLogMessage.length() > 1500) {
            //int i = chatLogMessage.indexOf("\n")
            chatLogMessage.delete(0, 500);
        }
    }




    public void sendStatusToBT(String status) {
        String statusMsg = "@<Status>@"+status+"@</Status>@";
        sendMessage(statusMsg);;
    }
    public void sendScreenToBT(String screen) {
        String screenMsg = "@<Screen>@" + screen + "@</Screen>@";
        sendMessage(screenMsg);
    }

    public void sendLogToBT() {
        if (chatLogMessage.length() > 0) {
            String msg = "@<Log>@" + chatLogMessage.toString() + "@</Log>@";
            chatLogMessage = new StringBuilder();
            sendMessage(msg);
        }
    }

    public void sendAlarmToBT(Alarm alarm) {
        if (chatLogMessage.length() > 0) {
            String msg = "@<ALM>@" + chatLogMessage.toString() + "@</ALM>@";
            sendMessage(msg);
            chatLogMessage = new StringBuilder();
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    public void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(MainActivity.getContext(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            //Log.d(TAG, "Write:"+message);
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            //mOutEditText.setText(mOutStringBuffer);
        }
    }

    public String getChatStatus() {
        String chatStatus = "no connected";
        if (mChatService != null) {
            if (mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
                chatStatus = "connected";
            }
        }
        return chatStatus;
    }

    public int getState() {
        if (mChatService != null) {
            return mChatService.getState();
        }
        return BluetoothChatService.STATE_NONE;
    }



    /**
     * Makes this device discoverable for 300 seconds (5 minutes).
     */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);

            mActivity.startActivity(discoverableIntent);
        }
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //FragmentActivity mActivity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            //setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            //mConversationArrayAdapter.clear();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            //setStatus(R.string.title_connecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            //setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    //mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    // TODO was mache ich mit Dingen die ich empfange?
                    Toast.makeText(MainActivity.getContext(), readMessage, Toast.LENGTH_SHORT).show();
                    //String cmd = readMessage.substring(0,1);
                    remoteControl(readMessage);

                    //mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
                    break;

            }
        }
    };

    /*
     * verarbeitet Meldungen, die via Bluetootjh Chat reinkommen
     */
    public void remoteControl(String cmd) {
        //writeLog(3, "ich habe Bluetooth Msg erhalten:" + cmd);
        String command = (cmd+"      ").substring(0,5).trim();
        switch (command) {
            case "ToAir":
                LogService.getInstance(mActivity).writeLnLog("forced by BT");
                break;
            case "-":
                mCompanionAudioService.changeVolume(-1);
                break;
            case "+":
                // lauter
                mCompanionAudioService.changeVolume(+1);
                break;
            case "LOUD":
                // Ton ein
                mCompanionAudioService.setLoud();
                break;
            case "SILEN":
                // ton aus
                mCompanionAudioService.setSilent();
                break;
            case "Start":
                startAdventureCompanion();
                break;
            case "Stop":
                stopAdventureCompanion();
                break;
            case "load:":
                String db = cmd.substring(5,13);
                String name = cmd.substring(14,cmd.length());
                switch (db) {
                    case "airtable":
                        writeBluetoothLog("Command load "+name+" von Airtable starten");
                        data().loadAnlageFromAirtable(name);
                        writeBluetoothLog("Command load "+name+" von Airtable erledigt");
                        Toast.makeText(MainActivity.getContext(), "Anlage "+name+" von airtable geladen", Toast.LENGTH_LONG).show();
                        break;
                    case "local-db":
                        writeBluetoothLog("Command load "+name+" von local DB starten");
                        data().loadAnlageFromDb(name);
                        Toast.makeText(MainActivity.getContext(), "Anlage "+name+" von datenbank geladen", Toast.LENGTH_LONG).show();
                        writeBluetoothLog("Command load "+name+" von local DB erledigt");
                        break;
                    case "settings":
                        data().loadSettingFromAirtable(name);
                        Toast.makeText(MainActivity.getContext(), "ACSettings "+name+" von airtable geladen", Toast.LENGTH_LONG).show();

                }
                break;
            case "save:":
                db = cmd.substring(5,13);
                name = cmd.substring(14,cmd.length());
                switch (db) {
                    case "airtable":
                        writeBluetoothLog("Command save "+name+" to Airtable starten");
                        data().saveAnlageToAirtable(name);
                        Toast.makeText(MainActivity.getContext(), "Anlage "+name+" nach airtable gespeichert", Toast.LENGTH_LONG).show();
                        writeBluetoothLog("Command save "+name+" to Airtable erledigt");
                        break;
                    case "local-db":
                        writeBluetoothLog("Command save "+name+"  to local-db starten");
                        data().saveAnlageToDb(name);
                        Toast.makeText(MainActivity.getContext(), "Anlage "+name+" in Datenbank gespeichert", Toast.LENGTH_LONG).show();
                        writeBluetoothLog("Command save "+name+"  to local-db erledigt");
                }
                break;
            case "svas:":
                db = cmd.substring(5,13);
                name = cmd.substring(14,cmd.length());
                switch (db) {
                    case "airtable":
                        writeBluetoothLog("Command save " + name + " as newName to Airtable starten");
                        data().saveAnlageAsToAirtable(name, "newName");
                        Toast.makeText(MainActivity.getContext(), "Anlage " + name + " nach airtable gespeichert", Toast.LENGTH_LONG).show();
                        writeBluetoothLog("Command save " + name + " as newName to Airtable erledigt");
                        break;
                }
                break;
            case "del :":
                db = cmd.substring(5,13);
                name = cmd.substring(14,cmd.length());
                data().deleteAnlageFromDb(name);
                Toast.makeText(MainActivity.getContext(), "Anlage "+name+" von Datenbank gelöscht", Toast.LENGTH_LONG).show();

                break;
            case "relo:":
                //db = cmd.substring(5,13);
                //name = cmd.substring(14,cmd.length());
                data().reloadAll();
                Toast.makeText(MainActivity.getContext(), "Alle Anlagen von Airtable nach Datenbank", Toast.LENGTH_LONG).show();
                break;
            case "gdao:":
                name = cmd.substring(5,cmd.length());
                Anlage anl = data().getAnlageByName(name);
                anl = data().anlageLaden(anl);
                String dao = data().createDAO(anl);
                sendMessage("@<DAO>@"+dao+"@</DAO>@");
                break;
            case "json:":
                Data data = Data.getInstance();
                if (data == null) {
                    data = new Data(mActivity);
                }
                int i = MainActivity.getCurrentAnlageIndex();
                // holt die Anlage am Index i und stellt sicher, dass sie geladen ist
                Anlage anlage = data.getAnlage(i);
                //String json = gson.toJsonTree(anlage);
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(anlage);

                sendMessage("@<JSON>@"+json+"@</JSON>@");
                Toast.makeText(MainActivity.getContext(), "Die Anlage "+anlage.getName()+" als json verschickt", Toast.LENGTH_LONG).show();
                break;
            case "stat:":
                // schicke mir den Status

                break;
        }

    }

    private void startAdventureCompanion() {
        Intent launchIntent = new Intent();
        launchIntent.setComponent(new ComponentName("com.condires.adventure.companion", "com.condires.adventure.companion.LocationTrackerActivity"));

        //Intent launchIntent = getApplicationContext().getPackageManager().getLaunchIntentForPackage("com.condires.adventure.companion");
        if (launchIntent != null) {
            String defaultAnlage = ACSettings.getInstance().getDefaultAnlage();
            int anlageIndex = Data.getInstance().getAnlageIndexByName(defaultAnlage);
            launchIntent.putExtra("TestOn", "0");
            launchIntent.putExtra("DarkScreen", true);
            launchIntent.putExtra("AnlageIndex", anlageIndex);
            launchIntent.putExtra("Stop", false);
            mActivity.startActivity(launchIntent);
            //finish();
        } else {
            Toast.makeText(MainActivity.getContext(), " launch Intent not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopAdventureCompanion() {
        Intent stopIntent = new Intent("com.condires.adventure.companion.LocationTrackerActivity.stop");
        mActivity.sendBroadcast(stopIntent);
        /*
        Intent intent = new Intent(Intent.ACTION_SEND);  //Action_Main heißt Hauptklasse
        Toast.makeText(MainActivity.getContext(), " Stop called", Toast.LENGTH_SHORT).show();
        intent.setComponent(new ComponentName("com.condires.adventure.companion", "com.condires.adventure.companion.MainActivity"));
        intent.putExtra("Stop", true);
        mActivity.startActivity(intent);

        Intent stopIntent = new Intent();
        stopIntent.setComponent(new ComponentName("com.condires.adventure.companion", "com.condires.adventure.companion.LocationTrackerActivity"));
        stopIntent.putExtra("Stop", true);
        mActivity.startActivity(stopIntent);
        */
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    com.condires.adventure.companion.logwrapper.Log.d(TAG, "BT not enabled");
                    Toast.makeText(MainActivity.getContext(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    //getActivity().finish();
                }
        }
    }

    /**
     * Establish connection with other device
     *
     * @param data   An {@link Intent} with {@link EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }


    public void start() {
        if (mBluetoothAdapter != null) {
            if (mChatService != null) {
                // Only if the state is STATE_NONE, do we know that we haven't started already
                if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                    // Start the Bluetooth chat services
                    mChatService.start();
                }
            }
        }
    }
    public void stop() {
        if (mBluetoothAdapter != null) {
            if (mChatService != null) {
                mChatService.stop();
            }
        }
    }
}
