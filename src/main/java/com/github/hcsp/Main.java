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
      if(processedLinks.contains(link)) {
        continue;
      }

      if (link.contains("sina.cn")
              && !link.contains("passport.sina.cn")
              && !link.contains("hotnews.sina.cn")
              && (link.contains("news.sina.cn") || "https://sina.cn".equals(link))) {
        if (link.startsWith("//")) {
          link = "https:" + link;
        }
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(link);
        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
          System.out.println(link);
          HttpEntity entity1 = response1.getEntity();
          Document document = Jsoup.parse(EntityUtils.toString(entity1));
          ArrayList<Element> aTags = document.select("a");
          for (Element aTag : aTags) {
            linkPool.add(aTag.attr("href"));
          }

          // 假如是详情页， 就存入数据库 否则什么都不做
          ArrayList<Element> articleTags = document.select("article");
          if (!articleTags.isEmpty()) {
            for (Element article : articleTags) {
              System.out.println(article.child(0).text());
            }
          }

          // 处理完之后 放到已处理的 set中
          processedLinks.add(link);
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else {
        // 这是我们不感兴趣的 不处理它
      }
    }


  }
}
