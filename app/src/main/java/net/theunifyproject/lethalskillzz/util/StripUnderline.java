package net.theunifyproject.lethalskillzz.util;

import android.text.Spannable;
import android.text.style.URLSpan;
import android.widget.TextView;

/**
 * Created by Ibrahim on 07/11/2015.
 */
public class StripUnderline {

    public static void stripUnderlines(TextView textView) {
        Spannable s = (Spannable)textView.getText();
        URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
        for (URLSpan span: spans) {
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            s.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL());
            s.setSpan(span, start, end, 0);
        }
        textView.setText(s);
    }
}
