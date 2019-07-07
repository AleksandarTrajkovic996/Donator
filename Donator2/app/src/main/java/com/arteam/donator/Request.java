package com.arteam.donator;

public class Request {
    public String forID, fromID, articleID;

    public Request(String forID, String fromID, String articleID) {
        this.forID = forID;
        this.fromID = fromID;
        this.articleID = articleID;
    }

    public String getForID() {
        return forID;
    }

    public void setForID(String forID) {
        this.forID = forID;
    }

    public String getFromID() {
        return fromID;
    }

    public void setFromID(String fromID) {
        this.fromID = fromID;
    }

    public String getArticleID() {
        return articleID;
    }

    public void setArticleID(String articleID) {
        this.articleID = articleID;
    }
}
