package com.github.tototoshi.play2.json4s.test

import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import play.api.mvc.Handler
import play.api.test.Helpers._
import play.api.test.TestServer

trait JsonTestServer {

  def withServer[T](port: Int)(routes: (String, String) => Handler)(block: WSClient => T): T = {
    val app = GuiceApplicationBuilder().routes({
      case (method, path) => routes(method, path)
    }).build()
    running(TestServer(port, app))(block(app.injector.instanceOf[WSClient]))
  }

}
