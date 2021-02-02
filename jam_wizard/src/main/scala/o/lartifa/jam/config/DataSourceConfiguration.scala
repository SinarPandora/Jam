package o.lartifa.jam.config

import io.getquill.{PostgresJdbcContext, SnakeCase}
import org.springframework.context.annotation.{Bean, Configuration, Lazy}

/**
 * 数据源配置
 * Author: sinar
 * 2021/1/21 00:41
 */
@Configuration
class DataSourceConfiguration {
  @Bean @Lazy def db(): PostgresJdbcContext[SnakeCase] =
    new PostgresJdbcContext(SnakeCase, "db")
}
