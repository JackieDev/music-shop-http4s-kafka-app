package kafka

import cats.effect._
import cats.implicits._
import fs2.kafka.vulcan.{Auth, AvroSerializer, AvroSettings, SchemaRegistryClientSettings}
import fs2.kafka._
import fs2.kafka.ProducerSettings
import models.MusicProduct

object NewMusicProductProducer {
  import models.MusicProduct.codecMusicProduct

  val topic = "music"

  val avroSettings = AvroSettings[IO](
    SchemaRegistryClientSettings[IO]("http://0.0.0.0:8081")
      .withAuth(Auth.Basic("username", "password")))


  def sendMessage(topic: String,
                  key: String,
                  message: MusicProduct,
                  producer: KafkaProducer[IO, String, MusicProduct]): IO[Unit] =
    producer.produce(ProducerRecords.one(ProducerRecord(topic, key, message))).flatten.void


  def apply[F[_]: ConcurrentEffect: ContextShift](avroSettings: AvroSettings[F]
                                                 ): fs2.Stream[F, KafkaProducer[F, String, MusicProduct]] = {

    implicit val serializer: RecordSerializer[F, MusicProduct] =
      AvroSerializer[MusicProduct].using(avroSettings)

    val producerSettings =
      ProducerSettings[F, String, MusicProduct](
        keySerializer = Serializer[F, String],
        valueSerializer = serializer
      ).withBootstrapServers("localhost:9092")

    producerStream[F]
      .using {
        producerSettings
          .withAcks(Acks.All)
      }
  }


}
