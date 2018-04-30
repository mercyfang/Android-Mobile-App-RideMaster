package FirebaseDatabase;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;

/**
 * Created by mercyfang on 4/10/18.
 */

@IgnoreExtraProperties
public final class User {

    public String email;
    public ArrayList<String> requestIds;

    // Default constructor required for calls to DataSnapshot.getValue(User.class).
    public User() {
    }

    public User(FirebaseUser user) {
        this.email = user.getEmail();
        requestIds = new ArrayList<>();
    }

    public void addRequest(String requestId) {
        requestIds.add(requestId);
    }


}
