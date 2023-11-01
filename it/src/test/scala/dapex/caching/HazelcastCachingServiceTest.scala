package dapex.caching

import cats.effect.IO
import dapex.caching.config.HazelcastConfig
import dapex.messaging.Method.SELECT
import dapex.test.DapexMessageFixture
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.testcontainers.containers.{GenericContainer, Network}
import org.testcontainers.utility.DockerImageName
import org.typelevel.log4cats.slf4j.Slf4jLogger
import cats.effect.unsafe.implicits.global
import org.scalatest.OptionValues
import scala.jdk.CollectionConverters._

class HazelcastCachingServiceTest
    extends AnyFlatSpec
    with Matchers
    with ScalaFutures
    with DapexMessageFixture
    with OptionValues {

  implicit val defaultPatience =
    PatienceConfig(timeout = Span(30, Seconds), interval = Span(100, Millis))

  private implicit def unsafeLogger = Slf4jLogger.getLogger[IO]
  private val HazelcastImage = "hazelcast/hazelcast:latest"
  private val clusterName = "test-cluster"

  private val hazelcastT = setUpHCastContainer()
  private val config = hazelcastT._1
  private val container = hazelcastT._2

  val sut = HazelcastCachingService[IO](config, "test-map")
  val message = getMessage(SELECT)

  it should "run Hazelcast container" in {
    val result = container.isRunning

    result shouldBe true
  }

  it should "save and retrieve a message" in {
    val result = (for {
      _ <- sut.saveMessage("key1", message)
      _ <- sut.saveMessage("key2", message)
      ret <- sut.getMessage("key1")
    } yield ret).unsafeToFuture()

    whenReady(result) { r =>
      r.isDefined shouldBe true
      r.value shouldBe message
    }
  }

  it should "return None when key is not in the system" in {
    val result = sut.getMessage("nonexistantkey").unsafeToFuture()

    whenReady(result) { r =>
      r.isDefined shouldBe false
    }
  }

  it should "return all the keys" in {
    val result = sut.map.keySet()

    result.size() shouldBe 2
    val keySet = result.asScala
    Set("key1", "key2").subsetOf(keySet) shouldBe true
  }

  private def setUpHCastContainer(): (HazelcastConfig, GenericContainer[Nothing]) = {
    val container: GenericContainer[Nothing] = new GenericContainer(
      DockerImageName.parse(HazelcastImage)
    ) {
      override def addFixedExposedPort(hostPort: Int, containerPort: Int): Unit =
        super.addFixedExposedPort(hostPort, containerPort)
    }
    container.withExposedPorts(5701)
    container.withEnv("HZ_CLUSTERNAME", clusterName)
    container.start()
    val host = container.getHost
    val port = container.getFirstMappedPort
    val config = HazelcastConfig(
      clusterName = clusterName,
      clusterAddress = host,
      ports = port.toString,
      outwardPort = "",
      authTokenTTL = 300L
    )
    (config, container)
  }
}
