package com.github.hcsp;

import java.sql.SQLException;

public interface CrawlerDao {
  boolean alreadyProcessed(String link) throws SQLException;

  String getNextLinkThenDelete() throws SQLException;

  void addLinksToNotProcessed(String href) throws SQLException;

  void addLinksToAlreadyProcessed(String href) throws SQLException;

  void deleteLink(String link) throws SQLException;

  void insertLink(String link, String sql) throws SQLException;

  String getNextLink() throws SQLException;

  void insertNews(News news) throws SQLException;
}
