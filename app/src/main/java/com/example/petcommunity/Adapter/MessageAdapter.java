package com.example.petcommunity.Adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.petcommunity.Message;
import com.example.petcommunity.R;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends ArrayAdapter<Message> {
    private List<Message> messages;
    private Context context;

    public MessageAdapter(Context context) {
        super(context, 0);
        this.context = context;
        this.messages = new ArrayList<>();
    }

    public void addMessage(Message message) {
        messages.add(message);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Message getItem(int position) {
        return messages.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_message, parent, false);

            holder = new ViewHolder();
            holder.messageTextView = convertView.findViewById(R.id.messageTextView);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Message message = getItem(position);

        if (message != null) {
            holder.messageTextView.setText(message.getContent());
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView messageTextView;
    }
}
