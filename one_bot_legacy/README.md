# one_bot_legacy

从 `picq_bot_x_fork` 吸收的事件 / 指令 / CQ 组件类型，包名改为 `o.lartifa.jam.ob`。

- **不含** `PicqHttpServer`：HTTP 入站由 `one_bot_runtime`（ZIO）负责
- 出站仍可经 `IcqHttpApi` 表面调用，由 `jam_self` 的 `OneBotIcqHttpApiAdapter` 转到 ZIO `OneBotHttpClient`
- `cc.moecraft.logger`（HyLogger）仍为外部依赖，未迁包

M3 #21：业务侧 import 已全部改为 `o.lartifa.jam.ob.*`；仓库不再把 `picq_bot_x_fork` 编进 `jam_self`。
