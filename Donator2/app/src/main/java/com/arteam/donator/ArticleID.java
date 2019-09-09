package com.arteam.donator;

import com.google.firebase.firestore.Exclude;
import org.jetbrains.annotations.NotNull;

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
