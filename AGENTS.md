# Jam Agent 约定

## 测试约定（M1 / #22）

- 框架：**ScalaTest**（`AnyFlatSpec` + `Matchers`，经 `JUnitRunner` 接入 `sbtn test` / `sbtn jam_self/test`）
- 策略：**跟改动补测**——每个 M1 子任务触及的纯逻辑 / 可注入边界必须有对应测试
- 不做 Testcontainers、不做完整 NapCat/OneBot 集成测试；生产冒烟须维护者同意
- `Playground.scala` 是本地实验入口，已从 test 源集排除

## 通用经验（重试后成功）

- Pekko 1.1.x 会把 `scala-library` 升到 2.13.14+：❌ 仍锁 2.13.10 且开 `-Xsource:3` 会把 leading-infix / package-prefix-implicits 变硬错误 → ✅ 对齐 `scala_version` 到 ≥2.13.14，并修 Tables 显式类型、行首 `++`/`+=`、json4s `jvalue2*` import
- sbt + Lombok + JDK 25：❌ `matching(lombok)` 取到 HyLogger 传递的 1.14.8 作 `-processorpath` 会 TypeTags CNFE；Zinc 进程内 javac 也易踩模块隔离 → ✅ `dependencyOverrides` 钉 1.18.48+、按版本选 jar，并用 forked javac + `-J--add-opens=jdk.compiler/...`
- Scala 3.9：Slick 生成的 `Row.tupled` 不存在 → 改成 `Row.apply.tupled`。`Future.sequence` 若整段表达式被期待成 `Unit`，集合类型会推失败 → 调用后补 `()`。sbt 2 里返回 `File` 或碰 Zinc/Flyway classpath 的任务用 `Def.uncached`，`installDist` 仍写到 `jam_self/target/install`
- Scala 3.9 + `zio-blocks-next-*`：❌ 仍用 ThisBuild `-source:3.0-migration` 且不加 `-experimental` → `given`/`derives` 与 `@experimental` 引用失败 → ✅ 新模块去掉 migration、加 `-experimental`；依赖该模块的 `jam_self` 同样加 `-experimental`
- jam_self 在 `-source:3.0-migration` 下：❌ 写 `given Conversion[A, B] = ...` 报 `expression expected but '[' found` → ✅ 用 `implicit def a2b(a: A): B` 并加 `import scala.language.implicitConversions`
- 本机生产 ArfiesBot 从 agent shell 拉起：❌ 命令结束后 `nohup java … &` 仍会被进程组收掉；LaunchAgent 会踩 Postgres.app trust 对话框 → ✅ 用常驻后台 shell（`sleep 9999999`）挂住父进程，或从 Terminal / 已放行 Postgres.app 的 LaunchAgent 启动
