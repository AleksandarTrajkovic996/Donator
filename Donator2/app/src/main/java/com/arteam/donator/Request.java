package com.arteam.donator;

public class Request extends RequestID{
    public String type, fromID, articleID;
    //type: active-ask, active-offer, passive
    public Request(){}

    public Request(String type, String fromID, String articleID) {
        this.type = type;
        this.fromID = fromID;
        this.articleID = articleID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
