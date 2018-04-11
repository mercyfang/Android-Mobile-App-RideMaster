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
    // TODO: date, time, location fields.

    // Default constructor required for calls to DataSnapshot.getValue(User.class).
    public User() {
    }

    public User(FirebaseUser user) {
        this.uId = user.getUid();
        this.email = user.getEmail();
    }
}
