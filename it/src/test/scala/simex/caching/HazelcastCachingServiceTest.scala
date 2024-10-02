package simex.caching

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import org.typelevel.log4cats.slf4j.Slf4jLogger
import simex.caching.config.HazelcastConfig
import simex.messaging.Datum
import simex.test.SimexTestFixture
import io.github.thediscprog.slogic.Xor
import org.typelevel.log4cats.SelfAwareStructuredLogger

class HazelcastCachingServiceTest
    extends AnyFlatSpec
    with Matchers
    with ScalaFutures
    with SimexTestFixture
    with OptionValues {

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(30, Seconds), interval = Span(100, Millis))

  private implicit def unsafeLogger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]
  private val HazelcastImage = "hazelcast/hazelcast:latest"
  private val clusterName = "test-cluster"

  private val hazelcastT = setUpHCastContainer()
  private val config = hazelcastT._1
  private val container = hazelcastT._2

  val sut = CachingService[IO](config, "test-map")
  val message = authenticationRequest.copy(
    data = authenticationRequest.data ++ Vector(
      Datum(
        "person",
        None,
        Xor.applyRight(
          Vector(
            Datum("name", None, Xor.applyLeft("John Smith")),
            Datum("title", None, Xor.applyLeft("Mr")),
            Datum(
              "address",
              None,
              Xor.applyRight(
                Vector(
                  Datum("street", None, Xor.applyLeft("The Street")),
                  Datum("postcode", None, Xor.applyLeft("AA1 1AA"))
                )
              )
            )
          )
        )
      )
    )
  )

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

  it should "delete by key" in {
    val result = (for {
      _ <- sut.deleteMessage("key2")
      message <- sut.getMessage("key2")
    } yield message).unsafeToFuture()

    whenReady(result) { r =>
      r.isDefined shouldBe false
    }
  }

  it should "handle delete message when key does not exist" in {
    val result = (for {
      r <- sut.deleteMessage("anotherkey")
    } yield r).unsafeToFuture()

    whenReady(result) { r =>
      r shouldBe ()
    }
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
