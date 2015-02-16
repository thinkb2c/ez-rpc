package com.ecfront.rpc.akka.server

case class AkkaRequest(method: String, path: String, parameter: Map[String, String], body: Any)