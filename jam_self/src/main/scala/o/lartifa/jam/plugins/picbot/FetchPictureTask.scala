package o.lartifa.jam.plugins.picbot

import java.util.Base64
import java.util.concurrent.ForkJoinPool

import ammonite.ops.PipeableImplicit
import cc.moecraft.logger.HyLogger
import o.lartifa.jam.common.util.MasterUtil
import o.lartifa.jam.database.temporary.Memory.database.db
import o.lartifa.jam.database.temporary.schema.Tables._
import o.lartifa.jam.model.tasks.JamCronTask
import o.lartifa.jam.plugins.picbot.APIResponse._
import o.lartifa.jam.plugins.picbot.FetchPictureTask.logger
import o.lartifa.jam.pool.JamContext
import upickle.default._

import scala.annotation.tailrec
import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
 * 获取并下载图片任务
 *
 * Author: sinar
 * 2020/7/26 00:36
 */
class FetchPictureTask(name: String) extends JamCronTask(name) {

  import o.lartifa.jam.database.temporary.Memory.database.profile.api._

  val API: String = s"https://api.lolicon.app/setu/?${apiKey}r18=2&num=10"

  private val encoder: Base64.Encoder = Base64.getEncoder

  protected var currentPool: Option[ForkJoinPool] = None

  /**
   * 执行定时任务内容
   *
   * @return 并发占位符
   */
  override def run()(implicit exec: ExecutionContext): Future[Unit] = async {
    MasterUtil.notifyMaster("图片更新任务开始")
    val (pool, content) = startWorkingPool()
    currentPool = Some(pool)
    // 每次更新（下载） 100 张图片
    val result = await(Future.sequence((1 to 10)
      .map(_ => doDownload()(content)
        .recover { case _ => logger.warning("本次API调用失败"); 0 })))
      .tapEach(count => logger.log(s"已添加：${count}张图片"))
      .sum
    logger.log(s"已添加：${result}张图片")
    MasterUtil.notifyMaster(s"图片更新任务结束，已添加：${result}张图片")
    Future.successful(())
  }


  /**
   * 完成后执行
   */
  override def postRun(): Unit = {
    currentPool.getOrElse(return).shutdown()
  }

  /**
   * 新建线程池 + 异步上下文
   * 因为定时任务线程池是无界的，放任下去可能导致内存溢出
   * 这里创建额外的最大五线程的线程池进行约束
   *
   * @return 线程池，上下文元祖
   */
  private def startWorkingPool(): (ForkJoinPool, ExecutionContext) = {
    val pool = new ForkJoinPool(10)
    (pool, ExecutionContext.fromExecutor(pool))
  }

  /**
   * 执行一次请求 + 下载任务
   *
   * @param exec 异步执行上下文
   * @return 影响行数
   */
  private def doDownload()(implicit exec: ExecutionContext): Future[Int] = async {
    val response = requests.get(API, check = false)
    if (response.statusCode == 429) {
      logger.warning("API请求次数已达上限")
      0
    } else if (response.statusCode == 200) {
      val dataList = read[Response](response.text()).data
      val inserts = await(Future.sequence(dataList.map(createSingleInsertTask))).flatten
      // Bulk insert
      await(db.run(DBIO.sequence(inserts))).sum
    } else {
      logger.warning("本次API调用失败")
      0
    }
  }

  /**
   * 下载图片并创建数据库插入操作任务
   *
   * @param data JSON 数据
   * @param exec 异步执行上下文
   * @return 插入操作
   */
  private def createSingleInsertTask(data: PicData)(implicit exec: ExecutionContext) = Future {
    downloadPicture(data.url).map(base64Data => {
      WebPictures.insertOrUpdate {
        WebPicturesRow(
          pid = data.pid,
          uid = data.uid,
          title = data.title,
          author = data.author,
          url = data.url,
          isR18 = data.r18,
          width = data.width,
          height = data.height,
          tags = data.tags.mkString("，"),
          base64Data = Some(base64Data)
        )
      }
    })
  }

  /**
   * 下载指定图片，每张图片最多尝试四次
   *
   * @param url   图片地址
   * @param retry 尝试次数
   * @return 图片数据
   */
  @tailrec
  private def downloadPicture(url: String, retry: Int = 0): Option[String] = {
    logger.debug(s"开始下载图片：$url")
    if (retry > 3) {
      logger.warning(s"图片下载失败且尝试次数过多，地址：$url")
      None
    } else Try(requests.get(url).bytes |> encoder.encodeToString) match {
      case Failure(_) => logger.debug(s"图片下载失败，正在重试：$url"); downloadPicture(url, retry + 1)
      case Success(value) => logger.debug(s"图片下载完成：$url"); Some(value)
    }
  }
}

object FetchPictureTask {
  private lazy val logger: HyLogger = JamContext.loggerFactory.get().getLogger(FetchPictureTask.getClass)
}
