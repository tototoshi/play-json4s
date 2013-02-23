package com.github.tototoshi.play2.json4s.test

import unfiltered.request._
import unfiltered.response._
import unfiltered.filter._


trait MockServer {

  def withMockServer[A](plan: Plan)(f: Int => A) {
    val server = unfiltered.jetty.Http.anylocal.filter(plan)
    val port = server.port

    try {
      server.start()
      f(port)
    } finally {
      server.stop()
    }
  }

}
