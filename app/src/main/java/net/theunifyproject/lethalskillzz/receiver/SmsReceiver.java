package net.theunifyproject.lethalskillzz.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.telephony.SmsMessage;
import android.util.Log;

import net.theunifyproject.lethalskillzz.activity.OTPActivity;
import net.theunifyproject.lethalskillzz.app.AppConfig;
import net.theunifyproject.lethalskillzz.service.HttpService;

/**
 * Created by Ibrahim on 27/10/2015.
 */
public class SmsReceiver  extends BroadcastReceiver  {

    private static final String TAG = SmsReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        final Bundle bundle = intent.getExtras();
    try {
        if (bundle != null) {
            Object[] pdusObj = (Object[]) bundle.get("pdus");
            for (Object aPdusObj : pdusObj) {
                SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) aPdusObj);
                String senderAddress = currentMessage.getDisplayOriginatingAddress();
                String message = currentMessage.getDisplayMessageBody();

                Log.e(TAG, "Received SMS: " + message + ", Sender: " + senderAddress);

                // if the SMS is not from our gateway, ignore the message
                if (!senderAddress.toLowerCase().contains(AppConfig.SMS_ORIGIN.toLowerCase())) {
                    return;
                }

                // verification code from sms
                String verificationCode = getVerificationCode(message);

                Log.e(TAG, "OTP received: " + verificationCode);



                if(OTPActivity.mUiHandler != null) {
                    Message msg = new Message();
                    msg.what = AppConfig.smsHandlerOtp;
                    msg.obj = verificationCode;
                    OTPActivity.mUiHandler.sendMessage(msg);
                }else {

                    Intent httpIntent = new Intent(context, HttpService.class);
                    httpIntent.putExtra("otp", verificationCode);
                    context.startService(httpIntent);
                }
            }
        }
    } catch (Exception e) {
        Log.e(TAG, "Exception: " + e.getMessage());
    }
}

    /**
     * Getting the OTP from sms message body
     * ':' is the separator of OTP from the message
     *
     * @param message
     * @return
     */
    private String getVerificationCode(String message) {
        String code = null;
        int index = message.indexOf(AppConfig.OTP_DELIMITER);

        if (index != -1) {
            int start = index + 2;
            int length = 6;
            code = message.substring(start, start + length);
            return code;
        }

        return code;
    }
}
