package com.github.hcsp;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class JdbcCrawlerDao implements CrawlerDao {
  private static final String JDBC_URL = "jdbc:h2:file:/Users/dsying/Projects/hcsp/29_crawler/crawler/target/crawlerNew";
  private static final String USERNAME = "root";
  private static final String PASSWORD = "root";

  private final Connection connection;

  @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
  JdbcCrawlerDao() {
    try {
      this.connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean alreadyProcessed(String link) throws SQLException {
    ResultSet resultSet = null;
    try (PreparedStatement ps = connection.prepareStatement("select link from links_already_processed where LINK = ?")) {
      ps.setString(1, link);
      resultSet = ps.executeQuery();
      return resultSet.next();
    } finally {
      if (resultSet != null) {
        resultSet.close();
      }

    }
  }

  @Override
  public String getNextLinkThenDelete() throws SQLException {
    // 从待处理池子中捞一个来处理
    String link = getNextLink();
    if (link == null) {
      return null;
    }
    // 处理完成后从池子包括数据库中删除
    deleteLink(link);
    return link;
  }

  @Override
  public void addLinksToNotProcessed(String href) throws SQLException {
    insertLink(href, "LINKS_TO_BE_PROCESSED");
  }

  @Override
  public void addLinksToAlreadyProcessed(String href) throws SQLException {
    insertLink(href, "links_already_processed");
  }

  @Override
  public void deleteLink(String link) throws SQLException {
     try (PreparedStatement ps = connection.prepareStatement("delete from LINKS_TO_BE_PROCESSED where LINK = ?")) {
      ps.setString(1, link);
      ps.execute();
    }
  }

  @Override
  public void insertLink(String link, String tableName) throws SQLException {
    String sql = "insert into" + tableName + " values (?)";
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, link);
      ps.execute();
    }
  }


  @Override
  public String getNextLink() throws SQLException {
    try (PreparedStatement ps = connection.prepareStatement("select link from links_to_be_processed limit 1");
         ResultSet resultSet = ps.executeQuery()) {
      while (resultSet.next()) {
        return resultSet.getString(1);
      }
    }
    return null;
  }

  @Override
  public void insertNews(News news) throws SQLException {
    try (PreparedStatement ps = connection.prepareStatement("insert into NEWS (TITLE, CONTENT, URL, CREATED_AT, MODIFIED_AT) values (?,?,?,NOW(),NOW())")) {
      ps.setString(1, news.getTitle());
      ps.setString(2, news.getContent());
      ps.setString(3, news.getUrl());
      ps.execute();
    }
  }
}
