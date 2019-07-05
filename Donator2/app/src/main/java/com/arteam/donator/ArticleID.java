package com.arteam.donator;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.annotations.NotNull;

public class ArticleID {

    @Exclude
    public String articleID;
    public int id;

    public <T extends ArticleID> T withId(@NotNull final String id, @NotNull int idA){
        this.articleID = id;
        this.id = idA;

        return (T) this;
    }
}
