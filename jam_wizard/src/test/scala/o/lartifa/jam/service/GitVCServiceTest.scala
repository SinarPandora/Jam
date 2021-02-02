package o.lartifa.jam.service

import better.files.Dsl._
import better.files._
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.nio.file.Paths
import scala.sys.process._

/**
 * GitVCServiceTest
 *
 * Author: sinar
 * 2021/1/29 22:10
 */
class GitVCServiceTest extends AnyFlatSpec with MockFactory with Matchers {
  private val resources: File = Paths.get("src", "test", "resources").toFile.toScala
  private val testDir: File = resources / "test_git_path"

  private val userService: UserService = mock[UserService]
  private val service: GitVCService = new GitVCService(userService)

  "首先" must "创建测试文件夹" in {
    mkdir(testDir)
    assert(testDir.exists)
  }

  "测试文件夹" should "被正确初始化" in {
    (userService.getSessionUsername _).expects().returning(Some("test_user"))
    val result = service.init(testDir.pathAsString)
    assert(result.isRight)
    assert((testDir / ".git").exists)
    Process("git log --format=%B -n 1", testDir.toJava).!!.trim should be("首次保存")
    Process("git diff", testDir.toJava).!!.trim should be("")
  }

  val testFile: File = testDir / "test.md"

  "测试文件" should "被正确创建，以用于接下来的测试" in {
    testFile.createFileIfNotExists()
    testFile < "# HELLO WORLD"
    assert(testFile.exists)
    testFile.lineIterator.next() should be("# HELLO WORLD")
  }

  "提交" should "成功" in {
    (userService.getSessionUsername _).expects().returning(Some("test_user"))
    val result = service.tag("首次保存", testDir.pathAsString)
    assert(result.isRight)
    Process("git log --format=%B -n 1", testDir.toJava).!!.trim should be("首次保存")
    Process("git diff", testDir.toJava).!!.trim should be("")
  }

  "如果文件被更改，并进行提交" should "成功提交更改" in {
    testFile << "\nupdate"
    (userService.getSessionUsername _).expects().returning(Some("test_user"))
    val result = service.tag("更新文件", testDir.pathAsString)
    assert(result.isRight)
    val iter = testFile.lineIterator
    iter.next()
    iter.next() should be("update")
    Process("git log --format=%B -n 1", testDir.toJava).!!.trim should be("更新文件")
    Process("git diff", testDir.toJava).!!.trim should be("")
  }

  "如果此时获取 tag" should "取到最新的 tag" in {
    val tag = service.currentTag(testDir.pathAsString).getOrElse(fail("无法获取当前的 tag"))
    tag.getFullMessage.trim should be("更新文件")
  }

  "最后" should "删除测试文件夹" in {
    rm(testDir)
    assert(testDir.notExists)
  }
}
