package o.lartifa.jam.plugins.story_runner

import com.bladecoder.ink.runtime.StoryState
import o.lartifa.jam.database.temporary.schema.Tables.StorySaveFileRow
import o.lartifa.jam.model.ChatInfo

import java.sql.Timestamp
import java.time.Instant

/**
 * 实例状态（内存）
 * 该类保存内存中当前 Story 实例的状态，并不直接参与数据库更新
 * （请使用 freeze 方法转换为存档），
 * 所以此类并不持有 ID
 *
 * Author: sinar
 * 2021/2/6 15:43
 */
case class RunnerState
(
  storyId: Long,
  data: StoryState,
  chatInfo: ChatInfo,
  name: Option[String] = None,
  recordTime: Timestamp = Timestamp.from(Instant.now()),
  isAutoSaved: Boolean = false,
) {
  /**
   * 冻结当前状态为存档
   *
   * 该方法生成的 ID 没有意义，仅作为占位符
   *
   * @return 冻结后的存档数据
   */
  def freeze(): StorySaveFileRow = {
    StorySaveFileRow(
      id = -1L,
      storyId = storyId,
      data = data.toJson,
      chatType = chatInfo.chatType,
      chatId = chatInfo.chatId,
      recordTime = recordTime,
      isAutoSaved = isAutoSaved,
      name = name
    )
  }
}

object RunnerState {
  /**
   * 将存档读取到状态
   *
   * @param record 数据库记录
   * @return 存档对象
   */
  def load(record: StorySaveFileRow): RunnerState = {
    val state = new StoryState()
    state.loadJson(record.data)
    RunnerState(
      storyId = record.storyId,
      data = state,
      chatInfo = ChatInfo(record.chatType, record.chatId),
      name = record.name,
      recordTime = record.recordTime,
      isAutoSaved = record.isAutoSaved
    )
  }
}
