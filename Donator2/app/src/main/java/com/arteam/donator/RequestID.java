package com.arteam.donator;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.annotations.NotNull;

public class RequestID {

    @Exclude
    public String requestID;
    public int id;

    public <T extends RequestID> T withId(@NotNull final String id, @NotNull int idR){
        this.requestID = id;
        this.id = idR;

        return (T) this;
    }
}
