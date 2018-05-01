package FirebaseDatabase;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.NoSuchElementException;

import edu.duke.compsci290.ridermaster.Activities.MatchResultActivity;

/**
 * Created by mercyfang on 4/10/18.
 */

public class FirebaseDatabaseReaderWriter {

    private static String TAG = "FDReaderWriter";

    private FirebaseUser firebaseUser;
    private DatabaseReference root;

    final String[] currRequestId = new String[1];

    public FirebaseDatabaseReaderWriter() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        root = FirebaseDatabase.getInstance().getReference();
    }

    /**
     * Reads the date table and finds the requestId in requests table and computes score to find
     * the best match.
     * If no matched requests in the table, throw NoSuchElementException.
     *
     * @param request
     */
    public void readDateAndRequest(final Request request) throws NoSuchElementException {
        String[] dateArray = request.date.split("/");
        final DatabaseReference datesRef =
                root.child("dates").child(dateArray[0]).child(dateArray[1]).child(dateArray[2]);
        final DatabaseReference requestsRef = root.child("requests");

        final int[] maxScore = new int[1];
        maxScore[0] = Integer.MIN_VALUE;
        final String[] matchRequestId = new String[1];
        final String[] currRequestId = new String[1];
        datesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChildren()) {
                    throw new NoSuchElementException();
                }
                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    // Reads the same requestId entry in requests table.
                    DatabaseReference requestRef = requestsRef.child(dateSnapshot.getKey());
                    // Computes score and update best match if needed.
                    int currScore = computeScore(request, requestRef, currRequestId);
                    if (currScore > maxScore[0]) {
                        maxScore[0] = currScore;
                        matchRequestId[0] = currRequestId[0];
                    }
                }

                // If no qualified user is found, throws exception.
                if (maxScore[0] == Integer.MIN_VALUE) {
                    throw new NoSuchElementException();
                }

                readUserEmailAndUpdateMatchResultActivity(matchRequestId[0]);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Firebase Date table read failed.");
            }
        });
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
                Log.d(TAG, "Failed to read value.", databaseError.toException());
            }
        });

        writeRideRequest(request);
        writeDate(request.date, requestId);
    }


    private void readUserEmailAndUpdateMatchResultActivity(String uId) {
//        final DatabaseReference usersRef = root.child("users").child(uId);
//        final String[] userEmail = new String[1];
//        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                userEmail[0] = (String) dataSnapshot.child("email").getValue();
//                MatchResultActivity.updateStatusTextView(userEmail[0]);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.d(TAG, "Failed to read value.", databaseError.toException());
//            }
//        });
    }

    public void deleteUserAndRideRequest(String uid, String requestId){
        DatabaseReference requestRef = root.child("users").child(uid).child(requestId);
        requestRef.setValue(null);
        requestRef.removeValue();
    }

    public void deleteDate(String date, String requestId){
        DatabaseReference datesRef = root.child("dates");
        datesRef.child(date).child(requestId).setValue(null);
        datesRef.child(date).child(requestId).removeValue();
    }

    public void deleteRideRequest(String requestID){
        //delete a match request from database

        DatabaseReference requestsRef = root.child("requests");
        DatabaseReference currRequestRef = requestsRef.child(requestID);

        currRequestRef.child("uId").removeValue();
        currRequestRef.removeValue();
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
        //      distanceFromUser
        //      destination
        //      distanceFromDest
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

    private int computeScore(final Request request,
                             final DatabaseReference requestRef,
                             final String[] currRequestId) {
        final int[] score = new int[1];
        requestRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Do no match again if already matched.
                boolean isMatched = (boolean) dataSnapshot.child("isMatched").getValue();
                if (isMatched) {
                    return;
                }
                // Do not match if same user.
                String uId = (String) dataSnapshot.child("uId").getValue();
                if (uId.equals(request.uId)) {
                    return;
                }

                // The current requestId to be stored if achieves max score.
                currRequestId[0] = (String) dataSnapshot.child("requestId").getValue();

                // First checks time within range. If endTime of current user request is
                // before a request startTime, or if startTime of current user request is after a
                // request endTime, then do not match the two requests.
                String startTime = (String) dataSnapshot.child("startTime").getValue();
                String endTime = (String) dataSnapshot.child("endTime").getValue();
                if (request.startTime.compareTo(endTime) >= 0
                        || request.endTime.compareTo(startTime) <= 0) {
                    return;
                }
                // Start time is the latest start time between the two.
                String start = request.startTime.compareTo(startTime) < 0
                        ? startTime : request.startTime;
                // End time is the earliest start time between the two.
                String end = request.endTime.compareTo(endTime) > 0
                        ? endTime : request.endTime;
                score[0] += Math.abs(start.compareTo(end));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Firebase Request table read request failed.");
            }
        });

        return score[0];
    }
}
