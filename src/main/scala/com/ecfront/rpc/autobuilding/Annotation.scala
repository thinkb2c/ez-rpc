package com.ecfront.rpc.autobuilding

import scala.annotation.StaticAnnotation


case class get(uri: String, http: Boolean, akka: Boolean) extends StaticAnnotation

case class post(uri: String, http: Boolean, akka: Boolean) extends StaticAnnotation

case class delete(uri: String, http: Boolean, akka: Boolean) extends StaticAnnotation

case class put(uri: String, http: Boolean, akka: Boolean) extends StaticAnnotation
