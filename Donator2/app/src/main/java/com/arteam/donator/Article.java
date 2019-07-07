package com.arteam.donator;

public class Article extends ArticleID {

    public String type, name, size, description;

    public Article(){
    }

    public Article(String type, String name, String size, String description) {
        this.type = type;
        this.name = name;
        this.size = size;
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
