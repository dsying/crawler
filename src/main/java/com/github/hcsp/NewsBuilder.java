package com.github.hcsp;

import java.sql.Timestamp;

public final class NewsBuilder {
  private String title;
  private String content;
  private String url;
  private Timestamp createdAt;
  private Timestamp modifiedAt;

  private NewsBuilder() {
  }

  public static NewsBuilder aNews() {
    return new NewsBuilder();
  }

  public NewsBuilder withTitle(String title) {
    this.title = title;
    return this;
  }

  public NewsBuilder withContent(String content) {
    this.content = content;
    return this;
  }

  public NewsBuilder withUrl(String url) {
    this.url = url;
    return this;
  }

  public NewsBuilder withCreatedAt(Timestamp createdAt) {
    this.createdAt = (Timestamp) createdAt.clone();
    return this;
  }

  public NewsBuilder withModifiedAt(Timestamp modifiedAt) {
    this.modifiedAt = (Timestamp) modifiedAt.clone();
    return this;
  }

  public News build() {
    News news = new News();
    news.setTitle(title);
    news.setContent(content);
    news.setUrl(url);
    news.setCreatedAt(createdAt);
    news.setModifiedAt(modifiedAt);
    return news;
  }
}
