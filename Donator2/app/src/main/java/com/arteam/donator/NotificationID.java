package com.arteam.donator;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.annotations.NotNull;

public class NotificationID {
    @Exclude
    public String notificationID;
    public int id;

    public <T extends NotificationID> T withId(@NotNull final String id, @NotNull int idN){
        this.notificationID = id;
        this.id = idN;

        return (T) this;
    }
}
