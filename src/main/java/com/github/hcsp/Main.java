package com.github.hcsp;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Main {
  private static final String JDBC_URL = "jdbc:h2:file:/Users/dsying/Projects/hcsp/29_crawler/crawler/target/crawlerNew";
  private static final String USERNAME = "root";
  private static final String PASSWORD = "root";

  @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
  public static void main(String[] args) throws SQLException {
    try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
      while (true) {
        ArrayList<String> linkPool = loadUrlsFromDatabase(connection, "select link from links_to_be_processed");
        Set<String> processedLinks = new HashSet<>(loadUrlsFromDatabase(connection, "select link from links_already_processed"));

        if (linkPool.isEmpty()) {
          break;
        }
        // 从待处理池子中捞一个来处理
        // 处理完成后从池子包括数据库中删除
        String link = linkPool.remove(linkPool.size() - 1);
        insertOrDeleteOneLinkIntoDatabase(connection, link, "delete from LINKS_TO_BE_PROCESSED where LINK = ?");

        // 如果已经处理过 则跳过
        if (processedLinks.contains(link)) {
          continue;
        }

        if (isInterestingLink(link)) {
          Document document = HttpGetAndParseHtml(link);
          // 把当前 文档中的a标签的链接放入 links_to_be_processed
          addLinksToNotProcessed(connection, document);
          // 如果当前页面时新闻页面 放入 news表中
          storeIntoDataBaseIfItIsNewsPage(document, connection, link);
          // 处理完之后 links_already_processed
          insertOrDeleteOneLinkIntoDatabase(connection, link, "insert into LINKS_ALREADY_PROCESSED values (?)");
        }
      }

    }

  }

  /**
   * 插入待处理链接 links_to_be_processed表
   * @param connection
   * @param document
   * @throws SQLException
   */
  private static void addLinksToNotProcessed(Connection connection, Document document) throws SQLException {
    for (Element aTag : document.select("a")) {
      String href = aTag.attr("href");
      insertOrDeleteOneLinkIntoDatabase(connection, href, "insert into LINKS_TO_BE_PROCESSED values (?)");
    }
  }

  /**
   * 插入或删除一条记录
   * @param connection
   * @param link
   * @param s
   * @throws SQLException
   */
  private static void insertOrDeleteOneLinkIntoDatabase(Connection connection, String link, String s) throws SQLException {
    try (PreparedStatement ps = connection.prepareStatement(s);) {
      ps.setString(1, link);
      ps.execute();
    }
  }

  /**
   * 获取 links_already_processed 或 links_to_be_processed 表中的 link
   * @param connection
   * @param sql
   * @return
   * @throws SQLException
   */
  private static ArrayList<String> loadUrlsFromDatabase(Connection connection, String sql) throws SQLException {
    ArrayList<String> results = new ArrayList<>();
    try (PreparedStatement ps = connection.prepareStatement(sql);
         ResultSet resultSet = ps.executeQuery()) {
      while (resultSet.next()) {
        results.add(resultSet.getString(1));
      }
    }
    return results;
  }

  /**
   * 如果是新闻页面则放入NEWS表中
   * @param document
   * @param connection
   * @param link
   * @throws SQLException
   */
  private static void storeIntoDataBaseIfItIsNewsPage(Document document, Connection connection, String link) throws SQLException {
    ArrayList<Element> articleTags = document.select("article");
    if (!articleTags.isEmpty()) {
      for (Element article : articleTags) {
        System.out.println(article.child(0).text());
        try (PreparedStatement ps = connection.prepareStatement("insert into NEWS (TITLE, CONTENT, URL, CREATED_AT, MODIFIED_AT) values (?,?,?,NOW(),NOW())")) {
          ps.setString(1, article.child(0).text());
          ps.setString(2, article.html());
          ps.setString(3, link);
          ps.execute();
        }
      }
    }
  }

  /**
   * 使用jsoup根据url解析生成Document文档
   * @param link 网址
   * @return
   */
  private static Document HttpGetAndParseHtml(String link) {
    if (link.startsWith("//")) {
      link = "https:" + link;
    }
    CloseableHttpClient httpclient = HttpClients.createDefault();
    HttpGet httpGet = new HttpGet(link);
    try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
      HttpEntity entity1 = null;
      if (response1 != null) {
        entity1 = response1.getEntity();
      }
      return Jsoup.parse(EntityUtils.toString(Objects.requireNonNull(entity1)));
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   *  是否为感兴趣的链接
   * @param link
   * @return
   */
  private static boolean isInterestingLink(String link) {
    return (isIndexPage(link) || isNewsPage(link)) && !isLoginPage(link);
  }

  private static boolean isIndexPage(String link) {
    return "https://sina.cn".equals(link);
  }

  private static boolean isNewsPage(String link) {
    return link.contains("news.sina.cn") && !link.contains("hotnews.sina.cn");
  }

  private static boolean isLoginPage(String link) {
    return link.contains("passport.sina.cn");
  }
}
