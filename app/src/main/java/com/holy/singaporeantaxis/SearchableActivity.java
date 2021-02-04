package com.holy.singaporeantaxis;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.holy.singaporeantaxis.adapters.UserAdapter;
import com.holy.singaporeantaxis.helpers.SQLiteHelper;
import com.holy.singaporeantaxis.models.User;

import java.util.List;

@SuppressWarnings("deprecation")
public class SearchableActivity extends ListActivity implements AdapterView.OnItemClickListener {

    public static final int REQUEST_SEARCH_USER = 100;

    private List<User> mUserList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(android.R.layout.list_content);

        // Get the intent, verify the action
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // User searched with query: Get the query and search & display the results
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            // User selected suggestion: Start home activity to show the selected user
            String selectedUserId = intent.getDataString();
            startHomeActivity(selectedUserId);
        }

        getListView().setOnItemClickListener(this);
    }

    // Process list view item click

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        // User selected specific user: Start home activity to show the selected user
        String selectedUserId = mUserList.get(position).getId();
        startHomeActivity(selectedUserId);
    }

    // Search users with the query and display it

    private void doMySearch(String query) {

        mUserList = SQLiteHelper.getInstance(this).SearchUsersSignedIn(query);

        UserAdapter adapter = new UserAdapter(mUserList);
        setListAdapter(adapter);
    }

    // Start home activity to show the selected user

    private void startHomeActivity(String userId) {

        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(HomeActivity.EXTRA_SEARCHED_USER_ID, userId);
        startActivity(intent);

    }


}