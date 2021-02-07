import cats.effect._
import fs2.kafka._
import fs2.Stream
import kafka.NewMusicProductProducer
import models.MusicProduct

import scala.concurrent.ExecutionContext.global

object ServerApp extends IOApp {

  implicit val ec = global

  def createKafkaProducer: Stream[IO, KafkaProducer[IO, String, MusicProduct]] = {
    for {
      producer <- NewMusicProductProducer[IO](NewMusicProductProducer.avroSettings)
    } yield producer
  }

  def stream: Stream[IO, ExitCode] =
    for {
      kafkaProducer <- createKafkaProducer
      server <- Server.runStream(kafkaProducer)
    } yield server


  def run(args: List[String]): IO[ExitCode] = {
//    stream.attempt.unsafeRunSync match {
//      case Left(e) =>
//        IO {
//          println("*** An error occurred! ***")
//          if (e ne null) {
//            println(e.getMessage)
//          }
//          ExitCode.Error
//        }
//      case Right(r) => r.compile.drain.as(ExitCode.Success)
//    }
    stream.compile.drain.as(ExitCode.Success)
  }


}

