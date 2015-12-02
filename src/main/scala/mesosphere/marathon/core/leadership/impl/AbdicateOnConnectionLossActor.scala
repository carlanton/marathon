package mesosphere.marathon.core.leadership.impl

import akka.actor.{ Actor, ActorLogging, Props }
import com.twitter.common.zookeeper.ZooKeeperClient
import mesosphere.marathon.LeadershipAbdication
import org.apache.zookeeper.{ WatchedEvent, Watcher }

object AbdicateOnConnectionLossActor {
  def props(zk: ZooKeeperClient, leader: LeadershipAbdication): Props = {
    Props(new AbdicateOnConnectionLossActor(zk, leader))
  }
}

class AbdicateOnConnectionLossActor(zk: ZooKeeperClient,
                                    leader: LeadershipAbdication) extends Actor with ActorLogging {

  private[impl] val watcher = new Watcher {
    val reference = self
    override def process(event: WatchedEvent): Unit = reference ! event
  }

  override def preStart(): Unit = {
    log.info("Register as ZK Listener")
    zk.register(watcher)
  }
  override def postStop(): Unit = {
    log.info("Unregister as ZK Listener")
    zk.unregister(watcher)
  }

  def disconnected(): Unit = {
    log.warning("ZooKeeper connection has been dropped. Abdicate Leadership.")
    leader.abdicateLeadership()
  }

  override def receive: Receive = {
    case event: WatchedEvent if event.getState == Watcher.Event.KeeperState.Disconnected => disconnected()
    case event: WatchedEvent => log.info(s"Received ZooKeeper Status event: $event")
  }
}
