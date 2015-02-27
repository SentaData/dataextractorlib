package com.senta.dataextractor;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.senta.dataextractor.interfaces.ISentaSecurePrefs;
import com.senta.dataextractor.utils.DataExtractorPreferences;


public class StWsDataExtractionService extends Service {

    private static final int SAMPLING_TIME = 30000;
    private static final String TAG = StWsDataExtractionService.class.getName();
    public static volatile int NetConnStatus = 0;
    private String username;
    private String password;
    private String url;

    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//        new Thread(new Runnable() {
//            public void run() {
//
//                OnCheckServices();
//            }
//        }).start();

        /*new Thread(new Runnable() {
            public void run(){

                OnCheckFreeMemory();
            }
        }).start();*/

        /*new Thread(new Runnable() {
            public void run(){

                OnCollectStatic();
            }
        }).start(); */
        username = intent.getStringExtra(Constants.USERNAME);
        password = intent.getStringExtra(Constants.PASSWORD);
        url = intent.getStringExtra(Constants.URL_PROPERTY);

        if (username == null || username.isEmpty() || password == null || password.isEmpty() || url == null || url.isEmpty()) {
            Log.e(TAG, "Username or password or url is missing.");
        } else {
            new Thread(new Runnable() {
                public void run() {

                    OnCollectDynamic();
                }
            }).start();
        }
        return START_STICKY;
    }

    protected void OnCheckServices() {

        while (true) {

            Log.d(TAG, "Checking Internet Services thread 5s");

            try {

                new Thread(new StWsServiceStatus(this)).start();
                Thread.sleep(5000);

            } catch (InterruptedException e) {

                e.printStackTrace();
            }
        }
    }


    protected void OnCheckFreeMemory() {

        while (true) {
            Log.d("Threads", "Extracting Free Memory thread 5s");
            try {
                new Thread(new StExtractFreeMemory(getApplicationContext(), url)).start();
                Thread.sleep(5000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void OnCollectStatic() {
        while (true) {
            Log.d("Senta-Secure", "Extracting Static Parameter thread 5s");
            try {
                new Thread(new StExtractStatic(getApplicationContext(), url, username, password)).start();
                Thread.sleep(SAMPLING_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void OnCollectDynamic() {
        while (true) {
            Log.d("Senta-Secure", "Extracting Dynamic Parameter thread every: " + SAMPLING_TIME / 1000 + " sec");
            try {
                final String structureXmlB64 = getString(R.string.WsXmlDynamicParams);
                ISentaSecurePrefs securePrefs = new DataExtractorPreferences();
                new StExtractDynamic(getApplicationContext(), structureXmlB64, url, securePrefs).run();
                Thread.sleep(SAMPLING_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
