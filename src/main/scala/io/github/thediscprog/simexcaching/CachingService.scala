package io.github.thediscprog.simexcaching

import cats.Applicative
import com.hazelcast.client.HazelcastClient
import com.hazelcast.client.config.ClientConfig
import com.hazelcast.map.IMap
import io.github.thediscprog.simexcaching.config.HazelcastConfig
import org.typelevel.log4cats.Logger

object CachingService {

  def apply[F[_]: Applicative: Logger](
      hzConfig: HazelcastConfig,
      mapName: String
  ): CachingServiceAlgebra[F] = {
    val clientConfig = getHzcConfiguration(hzConfig)
    configureHzcClient(clientConfig, hzConfig, mapName)
  }

  private def configureHzcClient[F[_]: Applicative: Logger](
      clientConfig: ClientConfig,
      config: HazelcastConfig,
      mapName: String
  ): CachingServiceAlgebra[F] = {
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
