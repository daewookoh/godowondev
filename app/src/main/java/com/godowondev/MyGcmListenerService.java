/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.godowondev;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";
    private Context mContext;

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);
        Log.d(TAG, "Token: " + RegistrationIntentService.token);
/*
        try {
            answerToServer(RegistrationIntentService.token, "godowondev");
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */


        sendNotification(message);

        // [END_EXCLUDE]
    }
    // [END receive_message]

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void answerToServer(String token, String app_name) throws IOException {
        URL url = new URL("http://t3.godowoncenter.com/sample/gcm_get_push_result.goc");

        // HTTP 접속 구하기
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // 리퀘스트 메소드를 POST로 설정
        conn.setRequestMethod("POST");

        // 연결된 connection 에서 출력도 하도록 설정
        conn.setDoOutput(true);

        // 요청 파라미터 출력
        // - 파라미터는 쿼리 문자열의 형식으로 지정 (ex) 이름=값&이름=값 형식&...
        // - 파라미터의 값으로 한국어 등을 송신하는 경우는 URL 인코딩을 해야 함.
        try (OutputStream out = conn.getOutputStream()) {
            out.write("secret_key=bdkfasd".getBytes());
            out.write("&".getBytes());
            out.write(("app_name=" + app_name).getBytes());
            out.write("&".getBytes());
            out.write(("token=" + token).getBytes());
        }

        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        // 접속 해제
        conn.disconnect();

    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        if(message.startsWith("[ADDITIONAL_MAIL")) {
            intent.putExtra("navi_type","additional_mail");
            //defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            defaultSoundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.wakeup);




            //sendSMS("01099989584", message);
        } else {
            intent.putExtra("navi_type","reservation_error");
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("자동 PUSH 알림")
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if(message.startsWith("[ADDITIONAL_MAIL_SMS")) {
            notificationBuilder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });
        }else if(message.startsWith("[ADDITIONAL_MAIL_RING")) {
            AudioManager audiomanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audiomanager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            notificationBuilder.setSound(defaultSoundUri);
        }


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Notification note = notificationBuilder.build();

        if(
                (message.startsWith("[ADDITIONAL_MAIL_SMS_KYS]")
                || message.startsWith("[ADDITIONAL_MAIL_RING_KYS]")
                || message.startsWith("[ADDITIONAL_MAIL_RING_ALL]"))
                && getResources().getString(R.string.member_name).equals("김용식")
        )
        {
            notificationManager.notify(0 /* ID of notification */, note);
        }

        if(
                (message.startsWith("[ADDITIONAL_MAIL_SMS_KDW]")
                || message.startsWith("[ADDITIONAL_MAIL_RING_KDW]")
                || message.startsWith("[ADDITIONAL_MAIL_RING_ALL]"))
                && getResources().getString(R.string.member_name).equals("고대우")
                )
        {
            notificationManager.notify(0 /* ID of notification */, note);
        }
        //note.flags = Notification.FLAG_INSISTENT; // 알림을 반복하고 싶을경우 설정
        //notificationManager.notify(0 /* ID of notification */, note);
    }

    private void sendSMS(String phoneNumber, String message) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }

}
