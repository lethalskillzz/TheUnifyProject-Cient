package net.theunifyproject.lethalskillzz.util;

import android.content.Context;
import android.content.Intent;

import net.theunifyproject.lethalskillzz.activity.LoginActivity;
import net.theunifyproject.lethalskillzz.app.PrefManager;

/**
 * Created by Ibrahim on 30/12/2015.
 */
public class Logout {

    private Context context;

    public Logout(Context context) {
        this.context = context;
    }

    public void logout() {
        PrefManager pref = new PrefManager(context);
        pref.clearSession();
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        context.startActivity(intent);
    }
}
