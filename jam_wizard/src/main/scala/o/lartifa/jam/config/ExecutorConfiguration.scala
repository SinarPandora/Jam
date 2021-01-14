package o.lartifa.jam.config

import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

/**
 * 线程池配置
 *
 * Author: sinar
 * 2021/1/14 22:07
 */
@Configuration
class ExecutorConfiguration {
  /**
   * 默认 IO 任务线程池
   *
   * @return 线程池执行器
   */
  @Bean(Array("io-executor"))
  def ioExecutor(): ThreadPoolTaskExecutor = {
    val executor = new ThreadPoolTaskExecutor
    val processors = Runtime.getRuntime.availableProcessors()
    executor.setCorePoolSize(processors * 2)
    executor.setMaxPoolSize(20)
    executor.setThreadNamePrefix("io-executor-")
    executor.setQueueCapacity(30)
    executor.setKeepAliveSeconds(120)
    executor.initialize()
    executor
  }

}
