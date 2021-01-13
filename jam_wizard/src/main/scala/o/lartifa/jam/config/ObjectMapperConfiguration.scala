package o.lartifa.jam.config

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

/**
 * Jackson 序列化设置
 * Author: sinar
 * 2021/1/13 23:10
 */
@Configuration
class ObjectMapperConfiguration extends Jackson2ObjectMapperBuilderCustomizer {
  override def customize(jacksonObjectMapperBuilder: Jackson2ObjectMapperBuilder): Unit = {
    jacksonObjectMapperBuilder.modules(DefaultScalaModule, new JavaTimeModule)
  }
}
