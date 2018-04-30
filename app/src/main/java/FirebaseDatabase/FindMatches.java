package FirebaseDatabase;

import java.util.NoSuchElementException;

/**
 * Created by mercyfang on 4/10/18.
 */

public class FindMatches {

    private FirebaseDatabaseReaderWriter reader;

    public FindMatches(FirebaseDatabaseReaderWriter reader) {
        this.reader = reader;
    }

    public User findMatches(Request request) throws NoSuchElementException {
        User user = null;
        reader.readDateAndRequest(request);
        return user;
    }
}
