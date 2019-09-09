package com.arteam.donator;


import com.google.firebase.firestore.Exclude;

import org.jetbrains.annotations.NotNull;

public class UserID {

    @Exclude
    public String userID;
    public int id;

    public <T extends UserID> T withId(@NotNull final String id, @NotNull int idU){
        this.userID = id;
        this.id = idU;

        return (T) this;
    }
}
