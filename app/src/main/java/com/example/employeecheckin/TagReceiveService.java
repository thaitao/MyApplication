package com.example.employeecheckin;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

//import com.google.gson.JsonSyntaxException;
//import com.twomintech.neo.androidcommon.GsonHelper;
//import com.twomintech.neo.common.objects.Action;
//import com.twomintech.neo.common.objects.JsonTransportObject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class TagReceiveService extends Service
{
    private static final String ACTION_USB_PERMISSION="com.jack.rfiddemo.USB_PERMISSION";
    private static final String TAG = TagReceiveService.class.getSimpleName();
//    private ReadingThread readingThread;
    private ServerSocket serverSocket;
    private boolean isRunning = true;

    RFIDPnPacket mRFIDPacket;
    int[] myCardNoData = new int[32];
    private String lastId;
    private String tempId;

    @Override
    public void onCreate()
    {
        try
        {
            serverSocket = new ServerSocket(8424);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

//        readingThread =  new ReadingThread();
//        readingThread.start();

        setupRFID();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        int flag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            flag = PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT;
        }
        else
        {
            flag = FLAG_UPDATE_CURRENT;
        }

        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, flag);

        CharSequence name = "Nail Manager Channel";
        String description = "Notification channel for nail manager";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel channel = new NotificationChannel("mychannelid", name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        Notification notification =
                new Notification.Builder(this, channel.getId())
                        .setContentTitle("Nail Salon Manager")
                        .setContentText("Foreground service is running")
//                        .setSmallIcon(R.mipmap.app_icon)
                        .setContentIntent(pendingIntent)
                        .setTicker("my ticker")
                        .build();

        startForeground(1, notification);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onDestroy()
    {
        isRunning = false;
        if (serverSocket != null)
        {
            try
            {
                serverSocket.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void setupRFID()
    {
        mRFIDPacket=new RFIDPnPacket((UsbManager) getSystemService(Context.USB_SERVICE), this,
                ACTION_USB_PERMISSION);

        new AsyncTask<Integer, Object, Integer>()
        {
            @Override
            protected void onPostExecute(Integer integer)
            {
                if (integer >= 0)
                {
                    scan();
                }
            }

            @Override
            protected Integer doInBackground(Integer... integers)
            {
                return mRFIDPacket.PN_RF_Set(0);
            }
        }.execute(0);
    }

    private void scan()
    {
        new AsyncTask<Integer, Object, String>()
        {
            @Override
            protected void onPostExecute(String id)
            {
                if (id != null && !id.isEmpty())
                {
                    Intent intent = new Intent("com.twomintech.neo.employeecheckin.TAG_RECEIVED");
                    intent.putExtra("TagId", id);
                    sendBroadcast(intent);
                    Toast.makeText(getApplicationContext(), "Read Card successful " + lastId, Toast.LENGTH_SHORT).show();
                }
                scan();
            }

            @Override
            protected String doInBackground(Integer... integers)
            {
                Arrays.fill(myCardNoData,0);
                int irr = mRFIDPacket.PN_RF_M1_Search(myCardNoData,5);

                tempId = USBOp.convert2String(myCardNoData, irr);

                if (tempId == null || tempId.isEmpty() || !tempId.equals(lastId))
                {
                    lastId = tempId;
                    return lastId;
                }
                else
                {
                    return null;
                }
            }
        }.execute(0);
    }

//    private class ReadingThread extends Thread
//    {
//
//        @Override
//        public void run()
//        {
//            while (isRunning)
//            {
//                byte[] buffer = new byte[10240];
//                try
//                {
//                    Socket socket = serverSocket.accept();
//                    int numByte = socket.getInputStream().read(buffer);
//                    buffer = Arrays.copyOf(buffer, Math.max(0, numByte));
//                    String string = new String(buffer);
//                    Log.d(TAG, "===---------------json string:" + string);
//                    JsonTransportObject jsonTransportObject = GsonHelper.fromJson(string, JsonTransportObject.class);
//
//                    if (jsonTransportObject != null)
//                    {
//                        if (Action.CUSTOMER_CHECKIN_LINK_SUCESSFUL == jsonTransportObject.getAction())
//                        {
//                            Intent intent = new Intent("com.twomintech.neo.employeecheckin.DEVICE_LINKED");
//                            intent.putExtra("DeviceIp", socket.getInetAddress().getHostAddress());
//                            sendBroadcast(intent);
//                        }
//                        else if (Action.CUSTOMER_CHECKIN == jsonTransportObject.getAction())
//                        {
//                            Intent intent = new Intent("com.twomintech.neo.customercheckin.REQUEST_RECEIVED");
//                            intent.putExtra("Request", jsonTransportObject.getJson());
//                            sendBroadcast(intent);
//                        }
//                        else if (Action.EMPLOYEE_CHECKIN == jsonTransportObject.getAction())
//                        {
//                            String tagId = jsonTransportObject.getJson();
////                            tagId = tagId.substring(0, 10);
//                            Intent intent = new Intent("com.twomintech.neo.employeecheckin.TAG_RECEIVED");
//                            intent.putExtra("TagId", tagId);
//                            sendBroadcast(intent);
//                        }
//                    }
//                    socket.close();
//                }
//                catch (IOException e)
//                {
//                    e.printStackTrace();
//                }
//                catch (JsonSyntaxException e)
//                {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
}
