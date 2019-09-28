package com.github.hcsp;

import java.sql.SQLException;

public interface CrawlerDao {
  boolean alreadyProcessed(String link) throws SQLException;

  String getNextLinkThenDelete() throws SQLException;

  void addLinksToNotProcessed(String href) throws SQLException;

  void addLinksToAlreadyProcessed(String href) throws SQLException;

  void insertOrDeleteOneLinkIntoDatabase(String link, String sql) throws SQLException;

  String getNextLink(String sql) throws SQLException;

  void storeIntoDataBaseIfItIsNewsPage(News news) throws SQLException;
}
