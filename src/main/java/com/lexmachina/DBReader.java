package com.lexmachina;

import org.sqlite.SQLiteDataSource;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.SQLiteTemplates;

/**
 * Simple DB reader class used to execute SQL queries.
 *
 * Usage is something like:
 *
 * DBReader dbReader = new DBReader();
 * SQLQuery query = dbReader.queryFactory().query()...
 */
public class DBReader {

  private final SQLQueryFactory queryFactory;

  public DBReader() {
    SQLTemplates templates = new SQLiteTemplates();
    Configuration configuration = new Configuration(templates);
    SQLiteDataSource dataSource = new SQLiteDataSource();
    dataSource.setUrl("jdbc:sqlite::resource:db.sqlite3");

    queryFactory = new SQLQueryFactory(configuration, dataSource);
  }

  public SQLQueryFactory queryFactory() {
    return queryFactory;
  }

}
