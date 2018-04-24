package FirebaseDatabase;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by mercyfang on 4/10/18.
 */

public class FirebaseDatabaseReaderWriter {

    private FirebaseUser firebaseUser;
    private DatabaseReference root;

    public FirebaseDatabaseReaderWriter() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        root = FirebaseDatabase.getInstance().getReference();
    }

    public void writeUserAndRideRequest(final Request request) {
        // Users table looks like:
        // users {
        //  uId1 {
        //      email
        //      requests {
        //          requestId1:true
        //          …
        //      }
        //  }
        //  uId2 {
        //      …
        //  }
        // }
        final DatabaseReference usersRef = root.child("users");

        // Uses Firebase push id as requestId.
        final String requestId = root.child("requests").push().getKey();
        request.setRequestId(requestId);

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String uId = firebaseUser.getUid();
                if (dataSnapshot.hasChild(uId)) {
                    usersRef.child(uId).child("requests").child(requestId)
                            .setValue(true);
                } else {
                    User user = new User(firebaseUser);
                    usersRef.child(uId).setValue(user);
                    usersRef.child(uId).child("requests").child(requestId)
                            .setValue(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("FDReaderWriter", "Failed to read value.", databaseError.toException());
            }
        });

        writeRideRequest(request);
        writeDate(request.date, requestId);
    }

    private void writeRideRequest(Request request) {
        // Requests table looks like:
        // requests {
        //  requestId1 {
        //      userId
        //      date
        //      startTime
        //      endTime
        //      location
        //      destination
        //      isMatched
        //  }
        //  requestId2 {
		//      ..
        //  }
        // }
        DatabaseReference requestsRef = root.child("requests");

        DatabaseReference currRequestRef = requestsRef.child(request.getRequestId());
        currRequestRef.setValue(request);
        currRequestRef.child("uId").setValue(firebaseUser.getUid());
    }

    private void writeDate(String date, String requestId) {
        // Date table looks like:
        // dates {
        //  date1 {
        //      requestId1:true
        //      requestId2:true
        //      ..
        //  }
        //  date2 {
        //      ..
        //  }
        // }
        DatabaseReference datesRef = root.child("dates");
        datesRef.child(date).child(requestId).setValue(true);
    }
}
