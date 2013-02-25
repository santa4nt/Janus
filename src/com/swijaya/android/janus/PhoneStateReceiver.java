package com.swijaya.android.janus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneStateReceiver extends BroadcastReceiver {
    
    private static final String TAG = "PhoneReceiver";
    
    public static final String EXTRA_PHONE_NUMBER = Intent.EXTRA_PHONE_NUMBER;
    public static final String EXTRA_PHONE_STATE = TelephonyManager.EXTRA_STATE;
    public static final int EXTRA_STATE_IDLE        = 0x00000000;
    public static final int EXTRA_STATE_RINGING     = 0x00000001;
    public static final int EXTRA_STATE_OFFHOOK     = 0x00000002;
    public static final int EXTRA_STATE_OUTGOING    = 0x00000004;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle bundle = intent.getExtras();
        if (bundle == null)
            return;
        
        String phoneNumber = null;
        
        // outgoing call
        if (action.equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            phoneNumber = bundle.getString(Intent.EXTRA_PHONE_NUMBER);
            assert (phoneNumber != null);
            onOutgoingCall(context, phoneNumber);
        }
        // incoming call, offhook, idle
        else if (action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            String state = bundle.getString(TelephonyManager.EXTRA_STATE);
            assert(state != null);
            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                phoneNumber = bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                onIncomingCall(context, phoneNumber);
            }
            else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                onIdle(context);
            }
            else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                onOffhook(context);
            }
        }
    }
    
    public void onIncomingCall(Context context, String phoneNumber) {
        Log.d(TAG, "Incoming call from " + phoneNumber);
        Intent intent = new Intent(context, PhoneStateService.class);
        intent.putExtra(EXTRA_PHONE_STATE, EXTRA_STATE_RINGING);
        intent.putExtra(EXTRA_PHONE_NUMBER, phoneNumber);
        context.startService(intent);
    }
    
    public void onOutgoingCall(Context context, String phoneNumber) {
        Log.d(TAG, "Outgoing call to " + phoneNumber);
        Intent intent = new Intent(context, PhoneStateService.class);
        intent.putExtra(EXTRA_PHONE_STATE, EXTRA_STATE_OUTGOING);
        intent.putExtra(EXTRA_PHONE_NUMBER, phoneNumber);
        context.startService(intent);
    }
    
    public void onIdle(Context context) {
        Log.d(TAG, "Idle");
        Intent intent = new Intent(context, PhoneStateService.class);
        intent.putExtra(EXTRA_PHONE_STATE, EXTRA_STATE_IDLE);
        context.startService(intent);
    }
    
    public void onOffhook(Context context) {
        Log.d(TAG, "Offhook");
        Intent intent = new Intent(context, PhoneStateService.class);
        intent.putExtra(EXTRA_PHONE_STATE, EXTRA_STATE_OFFHOOK);
        context.startService(intent);
    }

}
