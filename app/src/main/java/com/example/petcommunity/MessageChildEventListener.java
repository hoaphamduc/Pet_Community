package com.example.petcommunity;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

public class MessageChildEventListener implements ChildEventListener {
    private MessageListener listener;

    public MessageChildEventListener(MessageListener listener) {
        this.listener = listener;
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
        Message message = dataSnapshot.getValue(Message.class);
        if (message != null) {
            listener.onMessageReceived(message);
        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
        // Not used in this example
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        // Not used in this example
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
        // Not used in this example
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        // Not used in this example
    }
}

