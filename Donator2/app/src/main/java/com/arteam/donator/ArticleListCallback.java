package com.arteam.donator;

import java.util.List;
import java.util.Map;

public interface ArticleListCallback {

    void onCallback(List<Article> list);

    void onCallback(Article article);

    void onCallback(Map<Integer, Article> map);
}
