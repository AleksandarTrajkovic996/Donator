package com.arteam.donator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface FriendsListCallback {

    void onCallback();

    void onCallback(Map<Integer, User> l);
}
