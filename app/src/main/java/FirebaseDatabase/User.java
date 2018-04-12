package FirebaseDatabase;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by mercyfang on 4/10/18.
 */

@IgnoreExtraProperties
public final class User {
    private String uId;
    private String email;
    private String date;
    private String startTime;
    private String endTime;
    private String location;

    // Default constructor required for calls to DataSnapshot.getValue(User.class).
    public User() {
    }

    public User(FirebaseUser user) {
        this.uId = user.getUid();
        this.email = user.getEmail();
    }

    public void setRideInfo(String date, String startTime, String endTime, String location) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
    }
}
