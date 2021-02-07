import cats.effect._
import com.typesafe.config.ConfigFactory
import config._
import database.{FlywayDatabaseMigrator, MusicProductRepository}
import doobie.Transactor
import fs2.kafka.{AutoOffsetReset, ConsumerRecord, ConsumerSettings, KafkaProducer, RecordDeserializer, RecordSerializer, consumerStream}
import fs2.kafka.vulcan.{AvroDeserializer, AvroSerializer, AvroSettings, SchemaRegistryClientSettings}
import fs2.Stream
import kafka.NewMusicProductProducer
import models.MusicProduct
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze._
import pureconfig.loadConfigOrThrow
import routes.Routes

import scala.concurrent.ExecutionContext

object Server {

  def runStream(kafkaProducer: KafkaProducer[IO, String, MusicProduct])(implicit timer: Timer[IO],
                                                                        cs: ContextShift[IO],
                                                                        ec: ExecutionContext): Stream[IO, ExitCode] = {

    val migrator = new FlywayDatabaseMigrator

    val cfg = ConfigFactory.load(getClass().getClassLoader())

    def avroSettings: AvroSettings[IO] =
      AvroSettings {
        SchemaRegistryClientSettings[IO]("http://localhost:8081")
      }.withAutoRegisterSchemas(true)

    implicit val deserializer: RecordDeserializer[IO, MusicProduct] =
      AvroDeserializer[MusicProduct].using(avroSettings)

    implicit val serializer: RecordSerializer[IO, MusicProduct] =
      AvroSerializer[MusicProduct].using(avroSettings)

    def processRecord(record: ConsumerRecord[String, MusicProduct]): IO[(String, MusicProduct)] =
      IO.pure(record.key -> record.value)

    val consumerSettings =
      ConsumerSettings[IO, String, MusicProduct]
        .withAutoOffsetReset(AutoOffsetReset.Latest)
        .withBootstrapServers("localhost:9092")
        .withGroupId("group")

    def simpleConsumer(topic: String, musicProductRepository: MusicProductRepository[IO]): fs2.Stream[IO, (String, MusicProduct)] =
      consumerStream(consumerSettings)
        .evalTap(_.subscribeTo(topic))
        .flatMap(_.stream)
        .evalMap(commitable =>
          IO.pure(println(s"------------------ record from kafka: ${commitable.record}")) *>
            musicProductRepository.addProduct(commitable.record.value) *>
            processRecord(commitable.record))


    val stream = for {
      // database setup
      dbConfig      <- IO(loadConfigOrThrow[DatabaseConfig](cfg, "database"))
      ms            <- migrator.flywayMigrateDatabase(dbConfig)
      _             <- IO(println(s"Database Migration Result: $ms"))
      tx = Transactor.fromDriverManager[IO](dbConfig.driver, dbConfig.url, dbConfig.user, dbConfig.password)

      // routes setup
      musicRepo      = new MusicProductRepository(tx)

      publishKafkaMessage = (key: String, msg: MusicProduct) =>
        NewMusicProductProducer.sendMessage("music", key, msg, kafkaProducer)


      productRoutes  = new Routes(musicRepo, publishKafkaMessage)
      routes         = productRoutes.routes
      httpApp        = Router("/" -> routes).orNotFound

      // server setup with kafka consumer
      server         = BlazeServerBuilder[IO].bindHttp(7000, "localhost")
        .withHttpApp(httpApp)
        .serve
        .concurrently(simpleConsumer("music", musicRepo))

    } yield server


    stream.unsafeRunSync()
  }

}
