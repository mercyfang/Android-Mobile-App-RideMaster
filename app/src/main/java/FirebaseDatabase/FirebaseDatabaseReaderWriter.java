package FirebaseDatabase;

import android.location.Geocoder;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.NoSuchElementException;

import edu.duke.compsci290.ridermaster.Activities.HistoryActivity;
import edu.duke.compsci290.ridermaster.Activities.MatchResultActivity;
import edu.duke.compsci290.ridermaster.Activities.RideRequestActivity;

/**
 * Created by mercyfang on 4/10/18.
 */

public class FirebaseDatabaseReaderWriter {

    private static String TAG = "FDReaderWriter";

    private FirebaseUser firebaseUser;
    private DatabaseReference root;

    private int size = 0;
    private int currentIdx = 0;
    private int maxScore = Integer.MIN_VALUE;
    private String matchRequestId = "";
    private String currRequestId = "";

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

        datesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChildren()) {
                    throw new NoSuchElementException();
                }
                int count = 0;
                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()){
                    count++;
                }
                size = count;

                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    // Reads the same requestId entry in requests table.
                    DatabaseReference requestRef = requestsRef.child(dateSnapshot.getKey());
                    // Computes score and update best match if needed.
                    computeScore(request, requestRef);
                }

                // If no qualified user is found, throws exception.
//                if (maxScore[0] == Integer.MIN_VALUE) {
//                    throw new NoSuchElementException();
//                }
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

    public void deleteUserAndRideRequest(String uid, String requestId){
        DatabaseReference requestRef = root.child("users").child(uid).child("requests").child(requestId);
        requestRef.removeValue();
    }

    public void deleteDate(String date, String requestId){
        DatabaseReference datesRef = root.child("dates");
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

    private void readUserEmail(String uId) {
        final DatabaseReference usersRef = root.child("users").child(uId);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                InfoHolder.matchedUserEmail = (String) dataSnapshot.child("email").getValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Failed to read value.", databaseError.toException());
            }
        });
    }

    private void computeScore(final Request request, DatabaseReference requestRef) {
        requestRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentIdx++;
                if (currentIdx == size){
                    currentIdx = 0;
                }
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
                currRequestId = (String) dataSnapshot.child("requestId").getValue();
                String matchedUserId = (String) dataSnapshot.child("uId").getValue();

                // First checks time within range. If endTime of current user request is
                // First checks time within range. If endTime of current user request is
                // before a request startTime, or if startTime of current user request is after a
                // request endTime, then do not match the two requests.
                String startTime = (String) dataSnapshot.child("startTime").getValue();
                String endTime = (String) dataSnapshot.child("endTime").getValue();
                if (request.startTime.compareTo(endTime) >= 0
                        || request.endTime.compareTo(startTime) <= 0) {
                    return;
                }

                // Checks whether longitude and latitude is within range.
                String[] startLocation =
                        ((String) dataSnapshot.child("location").getValue()).split(";");
                double startLocLat = Double.parseDouble(startLocation[0]);
                double startLocLong = Double.parseDouble(startLocation[1]);
                double distanceFromUser = Double.parseDouble(
                        (String) dataSnapshot.child("distanceFromUser").getValue());

                String[] currUserStartLocation = request.location.split(";");
                double currUserLocLat = Double.parseDouble(currUserStartLocation[0]);
                double currUserLocLong = Double.parseDouble(currUserStartLocation[1]);
                double distanceFromCurrUser = Double.parseDouble(request.distanceFromUser);

                if (currUserLocLat < startLocLat - distanceFromUser
                        || currUserLocLat > startLocLat + distanceFromUser
                        || currUserLocLong < startLocLong - distanceFromUser
                        || currUserLocLong > startLocLong + distanceFromUser) {
                    return;
                }
                if (startLocLat < currUserLocLat - distanceFromCurrUser
                        || startLocLat > currUserLocLat + distanceFromCurrUser
                        || startLocLong < currUserLocLong - distanceFromCurrUser
                        || startLocLong > currUserLocLong + distanceFromCurrUser) {
                    return;
                }
                // Uses time to calculate score.
                // Start time is the latest start time between the two.
                String start = request.startTime.compareTo(startTime) < 0
                        ? startTime : request.startTime;
                // End time is the earliest start time between the two.
                String end = request.endTime.compareTo(endTime) > 0
                        ? endTime : request.endTime;
                int currScore = 0;
                currScore += Math.abs(start.compareTo(end));

                // Uses longitude and latitude of user's location to calculate score.
                // We use the inverse of the distance difference because if the distance between
                // two users is larg e, then the score should be small, and vice versa.
                currScore += (Math.pow(Math.abs(currUserLocLat - startLocLat), 2)
                        + Math.pow(Math.abs(currUserLocLong - startLocLong), 2)) * 10;
                if (currScore > maxScore) {
                    maxScore = currScore;
                    matchRequestId = currRequestId;
                }
                readUserEmail(matchedUserId);

                if (currentIdx == (size - 1)){
                    readUserEmail(matchedUserId);
                    currentIdx = 0;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Firebase Request table read request failed.");
            }
        });
    }

    public void matchNotification(String requestId){
        DatabaseReference curRef = root.child("requests").child(requestId);
        curRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("isMatched") == null){
                    return;
                }
                if (dataSnapshot.child("isMatched").getValue() == null){
                    return;
                }
                boolean isMatched = (boolean) dataSnapshot.child("isMatched").getValue();

                if (isMatched){
                    //TODO: jane MERCY do not pass found a match here
                    //you should be passing the user email
                    MatchResultActivity.updateStatusTextView("Found a match!");
                    Log.d(TAG, "**** isMatched changed!.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Firebase request match Nofification failed.");

            }
        });
    }


    public void addRequestHistory(final String uid){
        DatabaseReference curRef = root.child("users").child(uid).child("requests");


        curRef.addChildEventListener(new ChildEventListener() {


            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("tag", "children added in user request " + uid);
                String requestId = dataSnapshot.getKey();
                Log.d("tag", "the children requestId is " + requestId);
                addRequest(uid, requestId);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void addRequest(final String uid, final String requestId){
        Log.d("tag", "after call func " + requestId);

        DatabaseReference curRef = root.child("requests").child(requestId);
        curRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String userId = (String) dataSnapshot.child("uId").getValue();

                if (userId == null){
                    return;
                }

                String date = (String) dataSnapshot.child("date").getValue();
                int rMonth = Integer.parseInt(date.split("/")[0]);
                int rDate = Integer.parseInt(date.split("/")[1]);
                int rYear = Integer.parseInt(date.split("/")[2]);
                String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                Log.d("tag", "this is user id:" + userId + ", " + uid + date);



                if (! userId.equals(uid)){
                    Log.d("tag", "user id do not match up");
                }

                String startTime = (String) dataSnapshot.child("startTime").getValue();
                String endTime = (String) dataSnapshot.child("endTime").getValue();

                Log.d("tag", date + endTime);

                String myStartLoc = (String) dataSnapshot.child("location").getValue();
                double myStartingLat = Double.parseDouble(myStartLoc.split(";")[0]);
                double myStartingLng = Double.parseDouble(myStartLoc.split(";")[1]);

                String myEndLoc = (String) dataSnapshot.child("destination").getValue();
                double myDestinationLat = Double.parseDouble(myEndLoc.split(";")[0]);
                double myDestinationLng = Double.parseDouble(myEndLoc.split(";")[1]);

                boolean isMatched = (boolean) dataSnapshot.child("isMatched").getValue();



                String text = RideRequestActivity.requestInfoText(date, startTime, endTime,
                         myStartingLat,  myStartingLng,
                         myDestinationLat,  myDestinationLng);

                HistoryActivity.getInstance().addRequestButton(requestId, uid, date, text, isMatched);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }




}
