package com.github.hcsp;

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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;


public class Crawler{

  private CrawlerDao dao = new MyBatisCrawlerDao();

  public static void main(String[] args) {
    try {
      new Crawler().run();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void run() throws SQLException {
      String link;
      // 获取下一个链接
      while ((link = dao.getNextLinkThenDelete()) != null) {
        // 已处理 执行下一次循环
        if (dao.alreadyProcessed(link)) {
          continue;
        }
        // 不感兴趣的链接，不处理
        if (isInterestingLink(link)) {
          Document document = HttpGetAndParseHtml(link);
          // 把当前 文档中的a标签的链接放入 links_to_be_processed
          addLinksToNotProcessed(document);
          // 如果当前页面时新闻页面 放入 news表中
          storeIntoDataBaseIfItIsNewsPage(document, link);
          // 处理完之后 links_already_processed
          dao.addLinksToAlreadyProcessed(link);
        }
      }
  }

  private void addLinksToNotProcessed(Document document) throws SQLException {
    for (Element aTag : document.select("a")) {
      String href = aTag.attr("href");
      if (isUselessLink(href)) {
        continue;
      }
      dao.addLinksToNotProcessed(autoCompleteHTTP(href));
    }
  }

  private boolean isUselessLink(String href) {
    return href.startsWith("JavaScript") || href.startsWith("#") || href.equals("");
  }

  private void storeIntoDataBaseIfItIsNewsPage(Document document, String link) throws SQLException {
    ArrayList<Element> articleTags = document.select("article");
    if (!articleTags.isEmpty()) {
      for (Element article : articleTags) {
        System.out.println(article.child(0).text());
        News news = NewsBuilder.aNews()
                .withTitle(article.child(0).text())
                .withContent(getNewContent(article))
                .withUrl(link).build();
        dao.insertNews(news);
      }
    }
  }

  private static String getNewContent(Element article) {
    ArrayList<Element> pElements = article.select(".art_content .art_p");
    StringBuilder content = new StringBuilder();
    for (Element element : pElements) {
      content.append(element.text()).append('\n');
    }
    return content.toString();
  }

  private static Document HttpGetAndParseHtml(String link) {
    CloseableHttpClient httpClient = HttpClients.createDefault();
    HttpGet httpGet = new HttpGet(autoCompleteHTTP(link));
    try (CloseableHttpResponse response1 = httpClient.execute(httpGet)) {
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

  private static String autoCompleteHTTP(String link) {
    if (link.startsWith("//")) {
      link = "https:" + link;
    }
    return link;
  }

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
