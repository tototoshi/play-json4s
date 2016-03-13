/*
 * Copyright (C) 2009-2016 Lightbend Inc. <https://www.lightbend.com>
 */
package com.github.tototoshi.play2.json4s.core.internal

import akka.stream._
import akka.stream.scaladsl.Flow
import akka.stream.stage._
import akka.util.ByteString
import play.api._
import play.api.http.Status._
import play.api.http.{ HttpConfiguration, LazyHttpErrorHandler }
import play.api.libs.streams.Accumulator
import play.api.mvc._

import scala.concurrent.{ Future, Promise }
import scala.language.reflectiveCalls

/**
 * Default body parsers.
 */
trait BodyParsers {

  /**
   * Default body parsers.
   */
  object parse {

    private[core] def createBadResult(msg: String, statusCode: Int = BAD_REQUEST): RequestHeader => Future[Result] = { request =>
      LazyHttpErrorHandler.onClientError(request, statusCode, msg)
    }

    /**
     * Enforce the max length on the stream consumed by the given accumulator.
     */
    private[core] def enforceMaxLength[A](request: RequestHeader, maxLength: Long, accumulator: Accumulator[ByteString, Either[Result, A]]): Accumulator[ByteString, Either[Result, A]] = {
      val takeUpToFlow = Flow.fromGraph(new BodyParsers.TakeUpTo(maxLength))
      Accumulator(takeUpToFlow.toMat(accumulator.toSink) { (statusFuture, resultFuture) =>
        import play.api.libs.iteratee.Execution.Implicits.trampoline
        val defaultCtx = play.api.libs.concurrent.Execution.Implicits.defaultContext

        statusFuture.flatMap {
          case MaxSizeExceeded(_) =>
            val badResult = Future.successful(()).flatMap(_ => createBadResult("Request Entity Too Large", REQUEST_ENTITY_TOO_LARGE)(request))(defaultCtx)
            badResult.map(Left(_))
          case MaxSizeNotExceeded => resultFuture
        }
      })
    }

  }
}

/**
 * Defaults BodyParsers.
 */
object BodyParsers extends BodyParsers {
  private val logger = Logger(this.getClass)

  private val hcCache = Application.instanceCache[HttpConfiguration]

  private[core] def takeUpTo(maxLength: Long): Graph[FlowShape[ByteString, ByteString], Future[MaxSizeStatus]] = new TakeUpTo(maxLength)

  private[core] class TakeUpTo(maxLength: Long) extends GraphStageWithMaterializedValue[FlowShape[ByteString, ByteString], Future[MaxSizeStatus]] {

    private val in = Inlet[ByteString]("TakeUpTo.in")
    private val out = Outlet[ByteString]("TakeUpTo.out")

    override def shape: FlowShape[ByteString, ByteString] = FlowShape.of(in, out)

    override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[MaxSizeStatus]) = {
      val status = Promise[MaxSizeStatus]()
      var pushedBytes: Long = 0

      val logic = new GraphStageLogic(shape) {
        setHandler(out, new OutHandler {
          override def onPull(): Unit = {
            pull(in)
          }
          override def onDownstreamFinish(): Unit = {
            status.success(MaxSizeNotExceeded)
            completeStage()
          }
        })
        setHandler(in, new InHandler {
          override def onPush(): Unit = {
            val chunk = grab(in)
            pushedBytes += chunk.size
            if (pushedBytes > maxLength) {
              status.success(MaxSizeExceeded(maxLength))
              // Make sure we fail the stream, this will ensure downstream body parsers don't try to parse it
              failStage(new MaxLengthLimitAttained)
            } else {
              push(out, chunk)
            }
          }
          override def onUpstreamFinish(): Unit = {
            status.success(MaxSizeNotExceeded)
            completeStage()
          }
          override def onUpstreamFailure(ex: Throwable): Unit = {
            status.failure(ex)
            failStage(ex)
          }
        })
      }

      (logic, status.future)
    }
  }

  private[core] class MaxLengthLimitAttained extends RuntimeException(null, null, false, false)
}
