package com.holy.singaporeantaxis.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.holy.singaporeantaxis.R;
import com.holy.singaporeantaxis.models.User;

import java.util.List;
import java.util.Locale;

public class UserAdapter extends BaseAdapter {

    private List<User> userList;

    public UserAdapter(List<User> userList) {
        this.userList = userList;
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public Object getItem(int position) {
        return userList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_activated_2, parent, false);
        }

        User user = userList.get(position);

        TextView text1 = convertView.findViewById(android.R.id.text1);
        TextView text2 = convertView.findViewById(android.R.id.text2);

        String strIdGender = String.format(Locale.getDefault(),
                "%s (%s)",
                user.getId(),
                user.isMale() ? "male" : "female");
        text1.setText(strIdGender);

        text2.setText(user.getPhone());

        return convertView;
    }
}
