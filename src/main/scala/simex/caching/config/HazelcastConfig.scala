package simex.caching.config

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import pureconfig.{CamelCase, ConfigFieldMapping}
import pureconfig.generic.ProductHint

case class HazelcastConfig(
    clusterName: String,
    clusterAddress: String,
    ports: String,
    outwardPort: String,
    authTokenTTL: Long
)

object HazelcastConfig {
  implicit val hint = ProductHint[HazelcastConfig](ConfigFieldMapping(CamelCase, CamelCase))
  implicit val hzcConfigurationDecoder: Decoder[HazelcastConfig] = deriveDecoder
}
