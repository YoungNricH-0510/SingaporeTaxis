package com.holy.singaporeantaxis.providers;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.holy.singaporeantaxis.R;
import com.holy.singaporeantaxis.helpers.SQLiteHelper;
import com.holy.singaporeantaxis.helpers.UtilHelper;
import com.holy.singaporeantaxis.models.User;

import java.util.List;

public class UserSuggestionProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        // Get query string user passed
        String query = uri.getLastPathSegment();

        // Get user ids which starts with the query
        List<User> userList = SQLiteHelper.getInstance(getContext()).SearchUsersSignedIn(query);

        // Make matrix cursor that includes the result (user ids) : the system requires this format
        MatrixCursor matrixCursor = new MatrixCursor(new String[] {
                "_ID",
                SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_TEXT_2,
                SearchManager.SUGGEST_COLUMN_ICON_1,    // Icon mustn't have colorControl tint
                SearchManager.SUGGEST_COLUMN_INTENT_DATA
        });

        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            matrixCursor.addRow(new Object[] {
                    1 + i,
                    user.getId(),
                    user.getPhone(),
                    UtilHelper.resourceToUri(getContext(), R.drawable.ic_marker_person).toString(),
                    user.getId()
            });
        }

        // Return the matrix cursor (which is going to be shown to the user)
        return matrixCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
