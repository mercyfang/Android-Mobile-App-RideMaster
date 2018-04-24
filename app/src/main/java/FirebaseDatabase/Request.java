package FirebaseDatabase;

/**
 * Created by mercyfang on 4/21/18.
 */

public class Request {

    // Do not write requestId into Firebase Realtime Database.
    private String requestId;

    public String uId;
    public String date;
    public String startTime;
    public String endTime;
    public String location;
    public String destination;
    public boolean isMatched;

    public Request(String uId, String date, String startTime, String endTime, String location,
                   String destination) {
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

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestId() {
        return requestId;
    }
}
