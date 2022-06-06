package mathtrainer;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class Util {

    static String getTimeStringNow(long millis) {

        return getTimeString(0, millis);
    }

    static String getTimeStringDuration(long millis) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);

        int mHour = calendar.get(Calendar.HOUR);
        int mMin = calendar.get(Calendar.MINUTE);
        int mSec = calendar.get(Calendar.SECOND);
        int mMil = calendar.get(Calendar.MILLISECOND);

        String sh = fill(mHour);
        String sm = fill(mMin);
        String ss = fill(mSec);
        String ms = fillMilli(mMil);

        return sh + ":" + sm + ":" + ss + ":" + ms;
    }

    private static String getTimeString(int correct, long millis) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);

        int mHour;
        if (correct > 0) {
            mHour = calendar.get(Calendar.HOUR) - 1;
        } else {
            mHour = calendar.get(Calendar.HOUR_OF_DAY);
        }

        int mMin = calendar.get(Calendar.MINUTE);
        int mSec = calendar.get(Calendar.SECOND);
        int mMil = calendar.get(Calendar.MILLISECOND);

        String sh = fill(mHour);
        String sm = fill(mMin);
        String ss = fill(mSec);
        String ms = fillMilli(mMil);

        String f = "";//""[hh:mm:ss:ms] ";
        return f + sh + "." + sm + "." + ss + "." + ms;
    }

    static String getDateString(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        int mYear = calendar.get(Calendar.YEAR);
        // https://stackoverflow.com/questions/1755199/calendar-returns-wrong-month
        int mMonth = calendar.get(Calendar.MONTH) + 1; // ONLY month count from 0
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        return mYear + "." + Util.fill(mMonth) + "." + Util.fill(mDay);
    }

    private static String fillMilli(int s) {
        String ss = "" + s;
        if (s < 10) return "00" + ss;
        if (s < 100) return "0" + ss;

        return fill(s);
    }

    private static String fill(long s) {
        String ss = "" + s;
        if (s < 10) {
            return "0" + ss;
        }
        return ss;
    }

    public static String myFormatter(double val, int w, int d) {

        java.text.DecimalFormat fmt = new java.text.DecimalFormat();
        fmt.setMaximumFractionDigits(d);
        fmt.setMinimumFractionDigits(d);
        fmt.setGroupingUsed(false);
        String s = fmt.format(val);
        while (s.length() < w) {
            s = " " + s;
        }
        return s;
    }

    public static String myFormatter(int x, int w) {

        String s = "" + x;
        while (s.length() < w) {
            s = " " + s;
        }
        return s;
    }

    public static String millisToTimeString(long milliseconds) {

        final long dy = TimeUnit.MILLISECONDS.toDays(milliseconds);
        final long hr = TimeUnit.MILLISECONDS.toHours(milliseconds)
                - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(milliseconds));
        final long min = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
                - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliseconds));
        final long sec = TimeUnit.MILLISECONDS.toSeconds(milliseconds)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds));
        final long ms = TimeUnit.MILLISECONDS.toMillis(milliseconds)
                - TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(milliseconds));

        String str = fill(hr) + ":" + fill(min) + ":" + fill(sec);// + ":" + ms;

//        if( dy > 0 )
        str = dy + ":" + str;

        return str;
    }

    /// main for testing
    public static void main(String[] args) {

        double v = 9.35;
        System.out.println(myFormatter(v, 4, 2));

        System.out.println( myFormatter(100, 20) );
    }
}















