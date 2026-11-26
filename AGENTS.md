# Jam Agent 约定

## 测试约定（M1 / #22）

- 框架：**ScalaTest**（`AnyFlatSpec` + `Matchers`，经 `JUnitRunner` 接入 `./gradlew :jam_self:test`）
- 策略：**跟改动补测**——每个 M1 子任务触及的纯逻辑 / 可注入边界必须有对应测试
- 不做 Testcontainers、不做完整 NapCat/OneBot 集成测试；生产冒烟须维护者同意
- `Playground.scala` 是本地实验入口，已从 test 源集排除

## 通用经验（重试后成功）

- Pekko 1.1.x 会把 `scala-library` 升到 2.13.14+：❌ 仍锁 2.13.10 且开 `-Xsource:3` 会把 leading-infix / package-prefix-implicits 变硬错误 → ✅ 对齐 `scala_version` 到 ≥2.13.14，并修 Tables 显式类型、行首 `++`/`+=`、json4s `jvalue2*` import
