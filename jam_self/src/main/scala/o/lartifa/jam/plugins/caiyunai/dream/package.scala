package o.lartifa.jam.plugins.caiyunai

/**
 * 彩云小梦模块通用
 *
 * Author: sinar
 * 2021/2/19 12:00
 */
package object dream {
  /**
   * 彩云小梦相关 API
   * <p>
   * uid：虚拟用户 id（作者id），使用 getUid 获取，会话内唯一，应缓存
   * nid：文章 id，使用 save 获得，会话内唯一，应缓存
   * mid：小梦角色（AI模型）id，使用 listModels 获取，可缓存
   * xid：梦境（联想内容）id，从 dream 和 dreamLoop 两个方法中获取，并在方法间传递
   * </p>
   * 流程：
   * 初始化：getUid -> listModels -> getSignature
   * 写作并联想：写下一些内容 -> save -> dream -[循环执行直到有结果]> dreamLoop
   * 如果接受结果：realizingDream -> save -> 主要阶段1
   * 如果不接受结果：dream -[循环执行直到有结果]> dreamLoop
   */
  @deprecated
  object APIs {
    private val host: String = "http://if.caiyunai.com/v1/dream"
    /**
     * 获取 uid
     * _id 即为 uid，此 id 在会话内有效，应保存
     * 请求方法：POST
     * 请求体样例：{"ostype":""}
     * 返回体样例：{"status":0,"data":{
     *  "user":{"_id":"602f3914db8c7e4a66819d65","created_at":1613707540,"updated_text":1613707540,"type":"user",
     *    "name":"dream","visible":true,"password":"dream123","session_id":"baaaf88a-7267-11eb-921b-eeeeeeeeeeee",
     *    "session_expire":"2021-02-19T04:05:40.389735+00:00","config":null,"code":"","source":"dream","cyuid":"",
     *    "dream":true,"cyopenid":"","cyunionid":null,"nickname":null,"headimgurl":null,"ostype":""
     *   }}}
     */
    val getUid: String = s"$host/getUid"
    /**
     * 列出可用模型
     * 请求方法：POST
     * 请求体样例：{"ostype":""}
     * 返回体样例：{"status":0,"data":{"public_rows":[
     *    {"_id":"60094a2a9661080dc490f75a","uid":"","public":true,"mid":"60094a2a9661080dc490f75a","status":0,
     *      "name":"小梦0号","_type":"公开","_status":"转换完成","dreamid":"general","temperature":1,"description":null},
     *    {"_id":"601ac4c9bd931db756e22da6","uid":"","public":true,"mid":"601ac4c9bd931db756e22da6","status":0,
     *      "name":"小梦1号","_type":"公开","_status":"转换完成","dreamid":"general","temperature":0.9,"description":""},
     *    {"_id":"601f92f60c9aaf5f28a6f908","uid":"","public":true,"mid":"601f92f60c9aaf5f28a6f908","status":0,
     *      "name":"纯爱","_type":"公开","_status":"转换完成","dreamid":"general","temperature":0.9,"description":",纯爱,耽美"},
     *    {"_id":"601f936f0c9aaf5f28a6f90a","uid":"","public":true,"mid":"601f936f0c9aaf5f28a6f90a","status":0,
     *      "name":"言情","_type":"公开","_status":"转换完成","dreamid":"general","temperature":0.9,"description":",言情,yanqing"},
     *    {"_id":"60211134902769d45689bf75","uid":"","public":true,"mid":"60211134902769d45689bf75","status":0,
     *      "name":"科幻","_type":"公开","_status":"转换完成","dreamid":"general","temperature":1.0,"description":",科幻"}
     *  ]}}
     */
    val listModels: String = s"$host/model_list"
    /**
     * 获取签名（用途未知）
     * 请求方法：POST
     * 请求体样例：{"url":"http://if.caiyunai.com/dream/","ostype":""}
     * 返回体样例：a20c41e85a80bdda4f17f5192db09ad6ddfdf614&#95;&#95;1613707540510
     * 转义字符为两个下划线
     */
    val getSignature: String = "https://xiaoyiwechat.caiyunai.com/getSignature"

    /**
     * 保存文稿（官方建议不要高于1000字）
     * 首次保存时，nid 可以不传
     * 请求方法：POST
     * 请求体样例：{"content":"花匠打开了手机，","title":"手机","nid":"602f3a7cb499433a1a16a458","ostype":""}
     * 返回体样例：{"status":0,"data":{"nid":"602f3a7cb499433a1a16a458"}}
     */
    def save(uid: String): String = s"$host/$uid/novel_save"

    /**
     * 小云做梦（触发AI续写）
     * content每次要传输全部已写文本
     * 请求方法：POST
     * 请求体样例：{"nid":"602f3a7cb499433a1a16a458","content":"花匠打开了手机，","uid":"602f3914db8c7e4a66819d65",
     * "mid":"60094a2a9661080dc490f75a","title":"手机","ostype":""}
     * 返回体样例：{"status":0,"msg":"ok","data":{"xid":"602f3dfe84f40329800a3760"}}
     */
    def dream(uid: String): String = s"$host/$uid/novel_ai"

    /**
     * 梦境回环（获取续写内容）
     * 该请求循环调用，直到小梦返回三条联想结果
     * 请求方法：POST
     * 请求体样例：{"nid":"602f3a7cb499433a1a16a458","xid":"602f3dfe84f40329800a3760","ostype":""}
     * 返回体样例：
     *  计算中：{"status":0,"data":{"rows":[],"count":1}}
     *  计算完毕：{"status":0,"data":{"rows":[
     *      {"content":"第一条联想结果","_id":"602f3dfe84f40329800a3761","mid":"60094a2a9661080dc490f75a"},
     *      {"content":"第二条联想结果","_id":"602f3dfe84f40329800a3761","mid":"60094a2a9661080dc490f75a"},
     *      {"content":"第三条联想结果","_id":"602f3dfe84f40329800a3761","mid":"60094a2a9661080dc490f75a"}
     *    ],"count":0}}
     * 其中 _id 为 xid
     * 三条联想结果的 xid 相同，也就是说无论选择哪个结果，在使用 realizingDream 方法时都应该传入相同的 xid
     */
    def dreamLoop(uid: String): String = s"$host/$uid/novel_dream_loop"

    /**
     * 稳定故事线（将续写内容添加到文本）
     * 请求方法：POST
     * 请求体样例：{"xid":"602f3bc4b7c2286a158255ed","index":0,"ostype":""}
     * 返回体样例：{"status":0,"msg":"ok"}
     */
    def realizingDream(uid: String): String = s"$host/$uid/add_dream_content"
  }

  /**
   * 彩云小梦相关 API
   * <p>
   * codeid：登录时使用的临时 id，用于标记验证码的所有人
   * uid：虚拟用户 id（作者id）
   * nid：文章 id，使用 save 获得
   * mid：小梦角色（AI模型）id
   * xid：梦境（联想内容）id
   * </p>
   * 流程：
   * 初始化：modelList -> sendCaptcha -> phoneLogin
   * 写作并联想：写下一些内容 -> novelSave -> novelAI
   * 如果接受结果：addNode -> novelSave -> 主要阶段1
   */
  object API_V2 {
    val modelList: String = "https://if.caiyunai.com/v2/model/model_list"

    val sendCaptcha: String = "https://if.caiyunai.com/v2/user/phone_message"

    val phoneLogin: String = "https://if.caiyunai.com/v2/user/phone_login"

    def novelSave(uid: String): String = s"https://if.caiyunai.com/v2/novel/$uid/novel_save"

    def novelAI(uid: String): String = s"https://if.caiyunai.com/v2/novel/$uid/novel_ai"

    def addNode(uid: String): String = s"https://if.caiyunai.com/v2/novel/$uid/add_node"

  }

}
