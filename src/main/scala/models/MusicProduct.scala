package models

import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import vulcan.Codec
import cats.implicits._

case class MusicProduct(id: Int, name: String, description: String, price: String, category: String)

object MusicProduct {
  implicit val decode: Decoder[MusicProduct] = deriveDecoder[MusicProduct]
  implicit val encode: Encoder[MusicProduct] = deriveEncoder[MusicProduct]

  implicit val codecMusicProduct: Codec[MusicProduct] =
    Codec.record("MusicProduct", "music-shop", None){ f =>
      (
        f("id", _.id),
        f("name", _.name),
        f("description", _.description),
        f("price", _.price),
        f("category", _.category)
      ).mapN(MusicProduct(_, _, _, _, _))
    }
}
