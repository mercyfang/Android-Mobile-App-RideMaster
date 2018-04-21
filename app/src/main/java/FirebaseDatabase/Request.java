package FirebaseDatabase;

import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by mercyfang on 4/21/18.
 */

public class Request {
    private String requestId;
    private String uId;
    private String date;
    private String startTime;
    private String endTime;
    private String location;
    private String destination;
    private boolean isMatched;

    public Request(String uId, String date, String startTime, String endTime, String location,
                   String destination) {
        // Uses the request information combined for hashing to generate requestId to avoid
        // collision.
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] requestInfo =
                    (uId + date + startTime + endTime + location + destination).getBytes();
            requestId = digest.digest(requestInfo).toString();
        } catch (NoSuchAlgorithmException e) {
            Log.d("Request", "Failed to hash requestId.");
        }

        this.uId = uId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.destination = destination;
        isMatched = false;
    }

    public void setMatched(boolean isMatched) {
        this.isMatched = isMatched;
    }
}
