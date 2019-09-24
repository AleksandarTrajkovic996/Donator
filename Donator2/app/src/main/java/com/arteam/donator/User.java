package com.arteam.donator;



public class User extends UserID implements Comparable<User> {

    private String friendID;
    private String first_name;
    private String last_name;
    private String address;
    private String country;
    private String phone_number;
    private String points;
    private String latitude;
    private String longitude;

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }

    public User(){ }
    public User(String fid){friendID = fid; }

    public User(String first_name, String last_name, String address, String country, String phone_number, String fID) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.address = address;
        this.country = country;
        this.phone_number = phone_number;
        this.friendID = fID;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getFriendID() {
        return friendID;
    }

    public void setFriendID(String friendID) {
        this.friendID = friendID;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    @Override
    public int compareTo(User user) {
        return (int)(Integer.parseInt(this.points) - Integer.parseInt(user.getPoints()));
    }
}
