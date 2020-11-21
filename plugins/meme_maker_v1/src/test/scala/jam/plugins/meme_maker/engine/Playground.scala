package jam.plugins.meme_maker.engine

import jam.plugins.meme_maker.engine.MemeMakerAPI.generateApi

/**
 * Author: sinar
 * 2020/11/19 23:05
 */
object Playground extends App {
  println {
    requests.post(generateApi, data = "{\"id\":14,\"fillings\":{\"sentence0\":\"哈*5\",\"sentence1\":\"你的实力 根本斗不过我\",\"sentence2\":\"练好了早过来打吧\"}}").text()
  }
}
