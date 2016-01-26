package mesosphere.marathon

import org.rogach.scallop.ScallopConf
import scala.util.Try

class ZookeeperConfTest extends MarathonSpec {

  test("urlParameterGetParsed") {
    val url = "zk://host1:123,host2,host3:312/path"
    val opts = conf("--zk", url)
    assert(opts.zkURL == url)
    assert(opts.zkHosts == "host1:123,host2,host3:312")
    assert(opts.zkPath == "/path")
    assert(opts.zkUsername.isEmpty)
    assert(opts.zkPassword.isEmpty)
  }

  test("urlParameterWithAuthGetParsed") {
    val url = "zk://user1:pass1@host1:123,host2,host3:312/path"
    val opts = conf("--zk", url)
    assert(opts.zkURL == url)
    assert(opts.zkHosts == "host1:123,host2,host3:312")
    assert(opts.zkPath == "/path")
    assert(opts.zkUsername == Some("user1"))
    assert(opts.zkPassword == Some("pass1"))
  }

  test("wrongURLIsNotParsed") {
    assert(Try(conf("--zk", "zk://host1:foo/path")).isFailure, "No port number")
    assert(Try(conf("--zk", "zk://host1")).isFailure, "No path")
    assert(Try(conf("--zk", "zk://user@host1:2181/path")).isFailure, "No password")
    assert(Try(conf("--zk", "zk://:pass@host1:2181/path")).isFailure, "No username")
  }

  def conf(args: String*) = {
    val opts = new ScallopConf(args) with ZookeeperConf {
      //scallop will trigger sys exit
      override protected def onError(e: Throwable): Unit = throw e
    }
    opts.afterInit()
    opts
  }
}
