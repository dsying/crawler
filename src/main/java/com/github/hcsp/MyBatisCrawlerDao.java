package com.github.hcsp;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MyBatisCrawlerDao implements CrawlerDao{
  private SqlSession sqlSession;
  private static final String NAMESPACE = "com.github.hcsp.CrawlerDao";

  public MyBatisCrawlerDao() {
    String resource = "db/mybatis/mybatis-config.xml";
    try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
      SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
      this.sqlSession = sqlSessionFactory.openSession(true);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean alreadyProcessed(String link) {
    int count = sqlSession.selectOne(NAMESPACE + ".alreadyProcessed", link);
    return count > 0;
  }

  @Override
  public synchronized String getNextLinkThenDelete() {
    // 该方法执行了查询和删除两个操作， 多线程状态下有可能出现问题， 因此给方法添加 synchronized, 以当前实例对象作为锁
    String link = getNextLink();
    if (link == null) {
      return null;
    }
    deleteLink(link);
    return link;
  }

  @Override
  public void addLinksToNotProcessed(String href) {
    insertLink(href, "links_to_be_processed");
  }

  @Override
  public void addLinksToAlreadyProcessed(String href) {
    insertLink(href, "links_already_processed");
  }

  @Override
  public void deleteLink(String link) {
    sqlSession.delete(NAMESPACE + ".deleteLink", link);
  }

  @Override
  public void insertLink(String link, String tableName) {
    Map<String, Object> param = new HashMap<>();
    param.put("link", link);
    param.put("tableName", tableName);
    sqlSession.insert(NAMESPACE + ".insertLink", param);
  }


  @Override
  public String getNextLink() {
    return sqlSession.selectOne(NAMESPACE + ".getNextLink");
  }

  @Override
  public void insertNews(News news) {
    Map<String, Object> param = new HashMap<>();
    param.put("title", news.getTitle());
    param.put("content", news.getContent());
    param.put("url", news.getUrl());
    sqlSession.insert(NAMESPACE + ".insertNews", param);
  }
}
