package com.ecfront.rpc

import java.util.regex.Pattern

import org.scalatest._

import scala.collection.mutable

class RegexSpec extends FunSuite {

  test("Regex Test") {

    val (userPathR, userPathNamed) = getRegex("/user/:id/")
    val (addressPathR, addressPathNamed) = getRegex("/user/:id/:addr/post/")

    val p1 = getParameters(userPathR, userPathNamed, "/user/1/")
    assert(p1.get("id").get == "1")
    val p2 = getParameters(addressPathR, addressPathNamed, "/user/1/hangzhou/post/")
    assert(p2.get("id").get == "1")
    assert(p2.get("addr").get == "hangzhou")
    val p3 = getParameters(addressPathR, addressPathNamed, "/user/1/杭州/post/")
    assert(p3.get("addr").get == "杭州")

  }

  def getParameters(regex: Pattern, named: Seq[String], source: String) = {
    val parameters = collection.mutable.Map[String, String]()
    val matcher = regex.matcher(source)
    if (matcher.matches()) {
      named.foreach(name => parameters += (name -> matcher.group(name)))
    }
    parameters
  }

  def getRegex(path: String): (Pattern, Seq[String]) = {
    var pathR = path
    var named = mutable.Buffer[String]()
    """:\w+""".r.findAllMatchIn(path).foreach {
      m =>
        val name = m.group(0).substring(1)
        pathR = pathR.replaceAll(m.group(0), """(?<""" + name + """>[^/]+)""")
        named += name
    }
    (Pattern.compile("^" + pathR + "$"), named)
  }
}

