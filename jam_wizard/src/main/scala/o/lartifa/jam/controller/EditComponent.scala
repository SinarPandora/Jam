package o.lartifa.jam.controller

import o.lartifa.jam.common.prototype.AsyncComponent
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.web.bind.annotation.RestController


/**
 * 编辑相关 API
 *
 * Author: sinar
 * 2020/10/8 23:52
 */
@RestController
class EditComponent extends AsyncComponent {

}

object EditComponent {
  private val logger: Logger = LoggerFactory.getLogger(classOf[EditComponent])
}
