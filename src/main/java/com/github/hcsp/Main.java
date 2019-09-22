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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Main {
  public static void main(String[] args) {
    ArrayList<String> linkPool = new ArrayList<>();
    linkPool.add("https://sina.cn");

    Set<String> processedLinks = new HashSet<>();
    while (true) {
      if (linkPool.isEmpty()) {
        break;
      }
      // 移除最后一个
      String link = linkPool.remove(linkPool.size() - 1);
      // 如果已经处理过 则跳过
      if (processedLinks.contains(link)) {
        continue;
      }
      if (isInterestingLink(link)) {
        Document document = HttpGetAndParseHtml(link);
        document.select("a").stream().map(aTag -> linkPool.add(aTag.attr("href")));
        storeIntoDataBaseIfItIsNewsPage(document);
        // 处理完之后 放到已处理的 set中
        processedLinks.add(link);
      } else {
        // 这是我们不感兴趣的 不处理它
      }
    }


  }

  private static void storeIntoDataBaseIfItIsNewsPage(Document document) {
    ArrayList<Element> articleTags = document.select("article");
    if (!articleTags.isEmpty()) {
      for (Element article : articleTags) {
        System.out.println(article.child(0).text());
      }
    }
  }

  private static Document HttpGetAndParseHtml(String link) {
    if (link.startsWith("//")) {
      link = "https:" + link;
    }
    CloseableHttpClient httpclient = HttpClients.createDefault();
    HttpGet httpGet = new HttpGet(link);
    try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
      System.out.println(link);
      HttpEntity entity1 = response1.getEntity();
      return Jsoup.parse(EntityUtils.toString(entity1));
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
