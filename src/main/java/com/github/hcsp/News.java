package com.github.hcsp;

import java.sql.Timestamp;

public class News {
  private String title;
  private String content;
  private String url;
  private Timestamp createdAt;
  private Timestamp modifiedAt;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Timestamp getCreatedAt() {
    return (Timestamp) createdAt.clone();
  }

  public void setCreatedAt(Timestamp createdAt) {
    this.createdAt = (Timestamp) createdAt.clone();
  }

  public Timestamp getModifiedAt() {
    return (Timestamp) modifiedAt.clone();
  }

  public void setModifiedAt(Timestamp modifiedAt) {
    this.modifiedAt = (Timestamp) modifiedAt.clone();
  }
}
