/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothchat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.UUID;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothThread2 {
    // Debugging
    private static final String TAG = "BluetoothChatService";
    private static final boolean D = true;

    // Name for the SDP record when creating server socket
    //private static final String NAME_SECURE = "BluetoothChatSecure";
    //private static final String NAME_INSECURE = "BluetoothChatInsecure";

    // Unique UUID for this application
    //private static final UUID MY_UUID_SECURE =
    //   UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    //private static final UUID MY_UUID_INSECURE =
    //    UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    private static final UUID MY_UUID = 
        	//UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
        	UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    
    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    //private AcceptThread mSecureAcceptThread;
    //private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    public BufferedWriter buf;
    private static final int movavgnum = 50;//870/870; //1 min
    public double[] movavg = new double[movavgnum];    
    public boolean alarmOn = false;
/*    private String modelFilename = "sdcard/dropbox/Android/train.model";	
    private svm_model model = null;
    private int mover=0;
    private int outer=0;
    	
    private int[] normrange =   {271, 736,    	     224, 876,
    	     62, 689,    	     304, 768,    	     147, 691,    	     177, 708,
    	     136, 780,    	     192, 816,    	     192, 753,    	     352, 792,
    	     248, 828,    	     188, 705,    	     371, 764,    	     318, 888,
    	     229, 728,    	     323, 752,    	     236, 883,    	     240, 748,
    	     280, 664,    	     220, 808,    	     243, 771,    	     243, 847,
    	     224, 775,    	     312, 752,    	     280, 700,   	     214, 806,
    	     327, 728,    	     105, 721,    	     323, 831,    	     259, 728};
   
  	public int predict(svm_node[] x){
  		return (int) svm.svm_predict(model, x);
    	}
    	
    public void loadModel(String modelFilename) throws IOException{
    	
    	File file = new File(modelFilename);  
    	
    	model = svm.svm_load_model(file);
    		//System.out.println(model.rho[0]);
    }


	public svm_node[] createExample(String[] args){
    	
    	double[] doubleargs = normalize(args);
    	svm_node[] x = new svm_node[30];
    		for (int i = 0; i<30; i++)
    			{
    			x[i]=createNode(i+1,doubleargs[i]);
    			}
    		return x;
    	}
    	
    private svm_node createNode(int index, double value)
    	{
    	svm_node node = new svm_node();
    	node.index=index;
    	node.value=value;
    	return node;
    	}
    	
    private double[] normalize(String[] args )
    	{
    	double[] temp = new double[30];
    		for (int k=0;k<30;k++)
    		{	
    			temp[k]=(new Double(args[k])-normrange[2*k+1])/(normrange[2*k+1]-normrange[2*k]);
    		}
    		return temp;
    	}
    	    	
    public int classify(String[] args) throws IOException{
    		svm_node[] example = null;
    		example = createExample(args);
			
    		mover++;
    		if(mover>movavgnum)
    			{
    			outer = predict(example);
    			mover=0;
    			}
    		
    		return outer;
    }   	  */

    
    public double mean(double[] p)
    {
        double sum = 0;  // sum of all the elements
        for (int i=0; i<p.length; i++) {
            sum += p[i];
        }
        return sum / p.length;
    }    
    
    private void initmovavg()
    {
    for (int i=0; i <movavg.length; i++)
    	{ 	movavg[i] = 0; }
    }
    
    private int movingAverage(int newnum)
    	{
    	for (int i=1; i < movavg.length; i++)
    		{
    		movavg[i-1] = movavg[i];
    		}
    	movavg[movavg.length-1] = (double) newnum;
    	if (mean(movavg)>0.5)
    		{return 1; }
    	else
    		{return 0; }
    	}
    
    
    private String[] splitLines(String str)
    	{
 	   String[] lines = str.split("\r\n|\n\r|\r|\n", 2);
 	   return  lines;
 	   }    
    
    //public static String convertStreamToString(InputStream is) throws IOException {
        /*
         * To convert the InputStream to String we use the BufferedReader.readLine()
         * method. We iterate until the BufferedReader return null which means
         * there's no more data to read. Each line will appended to a StringBuilder
         * and returned as String.
         */
    /*	
        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } finally {
                is.close();
            }
            return sb.toString();
        } else {        
            return "";
        }
    } */   
    
    
	public void PopUp(String title, String message, int time)
		{
		//v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		alarmOn=true;
		AlertDialog.Builder alt_bld = new AlertDialog.Builder(null);

		alt_bld.setMessage("Did you fix your posture")
    	.setCancelable(false)
    	.setPositiveButton("Okay", new DialogInterface.OnClickListener()
    	{
    	public void onClick(DialogInterface dialog, int id)
    		{// Action for 'Yes' Button
    		alarmOn=false;
    		initmovavg();
    		}
    	})
    	.setNegativeButton("Thanks", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{//  Action for 'NO' Button
			alarmOn=true;
			dialog.cancel();
			}
		});
		AlertDialog alert = alt_bld.create();
		// Title for AlertDialog
		alert.setTitle("Posture Up!");
		// Icon for AlertDialog
		//alert.setIcon(R.drawable.icon);
	
		alert.show();
		//v.vibrate(time);	
		
		//CharSequence text = "Hello toast!";
		//int duration = Toast.LENGTH_SHORT;
		//Toast toast = Toast.makeText(, text, duration);					
	}
    
	
    public void newDataLog(String name)
    {       	
    	Date tdate = new Date();
    	File logFile = new File("sdcard/dropbox/posture project/"+name+"_"+tdate.getMonth()+"_"+tdate.getDate()+"_"+tdate.getYear()+".txt");
        try
        	{
              logFile.createNewFile();
              buf = new BufferedWriter(new FileWriter(logFile, true));
          	
              //initializing movavg
              initmovavg();
    		} 
           catch (IOException e)
           {              // TODO Auto-generated catch block
              e.printStackTrace();
           }
    }
    
    
    public void appendDataLog(BufferedWriter buf, String text)
    {       
       try
       {          //BufferedWriter for performance, true to set append to file flag
          buf.append(text);
       }
       catch (IOException e)
       {          // TODO Auto-generated catch block
          e.printStackTrace();
       }
    }    
    
    
    public void closeDataLog(BufferedWriter buf)
    {       
    	try {
		buf.close();
    	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
    	}
    }        
    
    
    /**
     * Constructor. Prepares a new BluetoothChat session.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     */
    public BluetoothThread2(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
               
    }

    /**
     * Set the current state of the chat connection
     * @param state  An integer defining the current connection state
     */
        
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(BluetoothChat.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state. */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */
    public synchronized void start() {
        if (D) Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        setState(STATE_NONE);

        /*
    	try {
			loadModel(modelFilename);
			Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString(BluetoothChat.TOAST, "Model Successfully Loaded");
            msg.setData(bundle);
            mHandler.sendMessage(msg);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
            Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString(BluetoothChat.TOAST, "Model Failed to load");
            msg.setData(bundle);
            mHandler.sendMessage(msg);
		}
        */
        
        // Start the thread to listen on a BluetoothServerSocket
        /*if (mSecureAcceptThread == null) {
            mSecureAcceptThread = new AcceptThread(true);
            mSecureAcceptThread.start();
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread(false);
            mInsecureAcceptThread.start();
        }*/
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     **/
    public synchronized void connect(BluetoothDevice device, boolean secure) {
        if (D) Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device, secure);
        mConnectThread.start();
        setState(STATE_CONNECTING);
        
        //datalog
        newDataLog("Simon_Map3_Unconnected");
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device, final String socketType) {
        if (D) Log.d(TAG, "connected, Socket Type:" + socketType);

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Cancel the accept thread because we only want to connect to one device
       /* if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }*/

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothChat.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);        
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

 /*       if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }*/
        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
    	setState(STATE_NONE);
    	// Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothChat.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        BluetoothThread2.this.start();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
    	setState(STATE_NONE);
    	closeDataLog(buf);
    	
    	// Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothChat.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        BluetoothThread2.this.start();
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
/*    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        public AcceptThread(boolean secure) {
            BluetoothServerSocket tmp = null;
            mSocketType = secure ? "Secure":"Insecure";

            // Create a new listening server socket
            try {
                if (secure) {
                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE,
                        MY_UUID_SECURE);
                } else {
                    tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(
                            NAME_INSECURE, MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            if (D) Log.d(TAG, "Socket Type: " + mSocketType +
                    "BEGIN mAcceptThread" + this);
            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket Type: " + mSocketType + "accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothChatService.this) {
                        switch (mState) {
                        case STATE_LISTEN:
                        case STATE_CONNECTING:
                            // Situation normal. Start the connected thread.
                            connected(socket, socket.getRemoteDevice(),
                                    mSocketType);
                            break;
                        case STATE_NONE:
                        case STATE_CONNECTED:
                            // Either not ready or already connected. Terminate new socket.
                            try {
                                socket.close();
                            } catch (IOException e) {
                                Log.e(TAG, "Could not close unwanted socket", e);
                            }
                            break;
                        }
                    }
                }
            }
            if (D) Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);

        }

        public void cancel() {
            if (D) Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket Type" + mSocketType + "close() of server failed", e);
            }
        }
    }*/


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;
        /*
        public ConnectThread(BluetoothDevice device, boolean secure) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                if (secure) {
                    tmp = device.createRfcommSocketToServiceRecord(
                            MY_UUID_SECURE);
                } else {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(
                            MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }
            mmSocket = tmp;
        }*/
        
        public ConnectThread(BluetoothDevice device, boolean secure) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            // Get a BluetoothSocket for a connection with the given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {}
            mmSocket = tmp;
        }
        
        
        public void run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
            	connectionFailed();
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", e2);
                }
                //connectionFailed();
                BluetoothThread2.this.start();
                return;
            }
            

            // Reset the ConnectThread because we're done
            synchronized (BluetoothThread2.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice, mSocketType);
        }

        public void cancel() {
            try {
                mmSocket.close();
                closeDataLog(buf);
            } catch (IOException e) {
                Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
             
        public ConnectedThread(BluetoothSocket socket, String socketType) {
            Log.d(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        
		public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes=1;
            String stringer = "";
            String[] lines;
            int stayclassy;
    		          
            
            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                	bytes = mmInStream.read(buffer);
                	
                    stringer = stringer + new String(buffer, 0, bytes);
                    
                    /*
                    while(countLines(stringer)<=1)
                		{
                    	bytes=mmInStream.read(buffer);
                    	stringer = stringer + new String(buffer, 0, bytes);	
                		}
                    bufre=new BufferedReader(new StringReader(stringer));
                    */
                    //messageline = bufre.readLine();
                    
                    // Send the obtained bytes to the UI Activity
                    
                	//stringer = convertStreamToString(mmInStream);
                    lines = splitLines(stringer);
                    while (lines.length > 1)
                    	{

                    	mHandler.obtainMessage(BluetoothChat.MESSAGE_READ, lines[0].getBytes().length, -1, lines[0].getBytes())
                           .sendToTarget();
                    	appendDataLog(buf, System.currentTimeMillis()+","+ lines[0]);

                       	stayclassy = 0;//classify(lines[0].split(","));

                    	
                    	//output
                       	
                    	// output
                    	if ((movingAverage(stayclassy)==1)&&(alarmOn==false))
                    		{
                            //PopUp("Hey", "Prop Up Your Posture!", 1000)
                            Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_POSTURE_BAD);
                            Bundle bundle = new Bundle();
                            bundle.putString(BluetoothChat.TOAST, "Correct Your Posture");
                            msg.setData(bundle);
                            mHandler.sendMessage(msg);
                    		
                    		appendDataLog(buf, ",1,"+Integer.toString(movavgnum)+"\r\n");
                    		initmovavg();
                    		}
                    	else if ((movingAverage(stayclassy)==0)&&(alarmOn==false))
                    		{
                    		appendDataLog(buf, ",0,"+Integer.toString(movavgnum)+"\r\n");                    		
                    		}
                    	
                		
                    	
                    	/*stringertemp="";
                    	for (int i=1; i < lines.length; i++ )
                    		{
                        	stringertemp = stringertemp + lines[i];                    		
                        	appendDataLog(buf, "stringertemp - " + stringertemp);
                    		}
                    	lines = splitLines(stringertemp);*/
                    	stringer = lines[1]; //get remainder
                    	lines = splitLines(stringer);
                    	}
                	
                    
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         **/
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(BluetoothChat.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
                closeDataLog(buf);
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }         
    }
   
}



