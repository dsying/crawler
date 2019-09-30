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
      this.sqlSession = sqlSessionFactory.openSession();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean alreadyProcessed(String link) {
    return sqlSession.selectOne(NAMESPACE + ".alreadyProcessed", link);
  }

  @Override
  public String getNextLinkThenDelete() {
    String link = getNextLink();
    if (link == null) {
      return null;
    }
    deleteLink(link);
    return link;
  }

  @Override
  public void addLinksToNotProcessed(String href) {
    insertLink(href, "LINKS_TO_BE_PROCESSED");
  }

  @Override
  public void addLinksToAlreadyProcessed(String href) {
    insertLink(href, "LINKS_ALREADY_PROCESSED");
  }

  @Override
  public void deleteLink(String link) {
    sqlSession.selectOne(NAMESPACE + ".deleteLink", link);
  }

  @Override
  public void insertLink(String link, String tableName) {
    Map<String, Object> param = new HashMap<>();
    param.put("link", link);
    param.put("tableName", tableName);
    sqlSession.selectOne(NAMESPACE + ".insertLink", param);
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
    sqlSession.selectOne(NAMESPACE + ".insertNews", param);
  }
}
