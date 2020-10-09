package o.lartifa.jam

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

/**
 * 果酱 GUI 工具启动类
 *
 * Author: sinar
 * 2020/10/8 22:41
 */
@SpringBootApplication
class WizardApp extends SpringApplication

object WizardApp {
  def main(args: Array[String]): Unit = {
    SpringApplication.run(classOf[WizardApp])
  }
}
