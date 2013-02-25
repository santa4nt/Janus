package com.swijaya.android.janus;

import java.util.Random;

public class PhoneCall {
        
    private static final Random random = new Random();
    
    private final int id;
    private final String phoneNumber;
    
    public PhoneCall(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        this.id = random.nextInt();
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public int getID() {
        return id;
    }
    
    @Override
    public String toString() {
        return String.format("<PhoneCall(%s),ID:0x%08X>", phoneNumber, id);
    }
    
}
