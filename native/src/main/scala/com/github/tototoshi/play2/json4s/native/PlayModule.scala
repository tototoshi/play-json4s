/*
 * Copyright 2013 Toshiyuki Takahashi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.tototoshi.play2.json4s.native

import javax.inject.Inject

import akka.stream.Materializer
import com.github.tototoshi.play2.json4s.core._
import org.json4s.native.Document
import play.api.http.{HttpErrorHandler, ParserConfiguration}
import play.api.inject.{Binding, Module}
import play.api.libs.Files.TemporaryFileCreator
import play.api.{Configuration, Environment}

class Json4s @Inject()(
    config: ParserConfiguration,
    errorHandler: HttpErrorHandler,
    materializer: Materializer,
    temporaryFileCreator: TemporaryFileCreator)
  extends Json4sParser[Document](
    org.json4s.native.JsonMethods,
    config,
    errorHandler,
    materializer,
    temporaryFileCreator
  )

class Json4sModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = Seq(
    bind[com.github.tototoshi.play2.json4s.Json4s].to[Json4s]
  )
}
