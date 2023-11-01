package dapex.caching

import cats.Applicative
import cats.syntax.all._
import com.hazelcast.client.HazelcastClient
import com.hazelcast.client.config.ClientConfig
import com.hazelcast.map.IMap
import dapex.caching.config.HazelcastConfig
import dapex.messaging.DapexMessage
import io.circe.parser._
import io.circe.syntax._
import org.typelevel.log4cats.Logger

import java.util.concurrent.TimeUnit

case class HazelcastCachingService[F[_]: Applicative: Logger](
    map: IMap[String, String],
    ttl: Long
) extends CachingServiceAlgebra[F] {

  override def saveMessage(key: String, dapexMessage: DapexMessage): F[Unit] =
    for {
      _ <- Logger[F].info(s"Hzcast Saving Token: $key")
      _ = map.put(key, dapexMessage.asJson.noSpaces, ttl, TimeUnit.SECONDS)
    } yield ()

  override def getMessage(key: String): F[Option[DapexMessage]] =
    Logger[F].info(s"Hzcast Getting by token: $key") *>
      getDapexMessageFromCache(key, map)

  private def getDapexMessageFromCache(
      key: String,
      map: IMap[String, String]
  ): F[Option[DapexMessage]] =
    if (map.containsKey(key)) {
      val jsonStr = map.get(key)
      decode[DapexMessage](jsonStr) match {
        case Left(e) =>
          Logger[F].warn(s"Hzcast decoding problem: ${e.getMessage}") *>
            (None: Option[DapexMessage]).pure[F]
        case Right(value) => (Some(value): Option[DapexMessage]).pure[F]
      }
    } else {
      Logger[F].warn(s"Hzcast Key [$key] not found in map: ${map.getName}") *>
        (None: Option[DapexMessage]).pure[F]
    }
}

object HazelcastCachingService {

  def apply[F[_]: Applicative: Logger](
      hzConfig: HazelcastConfig,
      mapName: String
  ): HazelcastCachingService[F] = {
    val clientConfig = getHzcConfiguration(hzConfig)
    configureHzcClient(clientConfig, hzConfig, mapName)
  }

  private def configureHzcClient[F[_]: Applicative: Logger](
      clientConfig: ClientConfig,
      config: HazelcastConfig,
      mapName: String
  ): HazelcastCachingService[F] = {
    val hzClient = HazelcastClient.newHazelcastClient(clientConfig)
    val map: IMap[String, String] = hzClient.getMap[String, String](mapName)
    HazelcastCachingService(map, config.authTokenTTL)
  }

  private def getHzcConfiguration(config: HazelcastConfig): ClientConfig = {
    val clientConfig = new ClientConfig()
    clientConfig.setClusterName(config.clusterName)
    clientConfig.getNetworkConfig.addAddress(s"${config.clusterAddress}:${config.ports}")
    clientConfig
  }
}
