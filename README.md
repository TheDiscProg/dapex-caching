# SIMEX Cluster Caching Service

## Overview
This library provides a cluster-wide caching service that can be used throughout the project. The caching service uses Hazelcast.

## Configuration
The class `HazelcastConfig.scala` requires the following settings in `caching` property:
```
caching {
            clusterName: "shareprice",
            clusterAddress: "hazelcast",
            ports: "5701",
            outwardPort: "34700-34710",
            authTokenTTL: 3000
    }
```
Obviously, replace the values above with your values.

## Hazelcast Caching Service
To create a service, use:
```scala
   CachingService(hzConfig: HazelcastConfig, mapName: String): CachingServiceAlgebra[F]
```
This will create a map in Hazelcast cluster with the correct name and TTL, if one does not already exist.

This service has the following self-explanatory methods:

```scala
def saveMessage(key: String, message: Simex): F[Unit]

def getMessage(key: String): F[Option[Simex]]

def deleteMessage(key: String): F[Unit]
```

## Hazelcast Cluster
This can be embedded in the service by creating a local Hazelcast cluster or a system-wide defined cluster.