package net.theunifyproject.lethalskillzz.service;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by Ibrahim on 02/12/2015.
 */
public class GcmInstanceIDListenerService extends InstanceIDListenerService {

    @Override
    public void onTokenRefresh() {
        // Fetch updated Instance ID token and notify of changes

        Intent intent = new Intent(this, GcmRegistrationService.class);
        startService(intent);
    }

}
