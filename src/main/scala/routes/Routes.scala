package routes

import cats.effect.Sync
import cats.implicits._
import database._
import models._
import fs2.Stream
import io.circe.Json
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._

import org.http4s.dsl.Http4sDsl

class Routes[F[_]: Sync](repo: MusicProductRepository[F]) extends Http4sDsl[F] {

  implicit def decodeProduct: EntityDecoder[F, MusicProduct] = jsonOf


  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "status" =>
      Ok()

    // View all items in the products table
    case GET -> Root / "products" => {
      val ps = repo.loadProducts()
        .collect {
          case p: MusicProduct => p
        }
        .map(s => s.asJson)
      val result: Stream[F, Json] = ps
      Ok(result)
    }


    // Search products in the database with a search string
    case GET -> Root / "products" / "search" / search => {
      println(s"Call received to search for $search")
      val ps = repo.searchProducts(search)
        .collect {
          case p: MusicProduct => p
        }
        .map { s =>
          println(s"Sending this to frontend: $s")
          s.asJson
        }
      val result: Stream[F, Json] = ps
      Ok(result)
    }


    // View all items in specified category
    case GET -> Root / "products" / category => {
      val ps = repo.loadProductsByCategory(category)
        .collect {
          case p: MusicProduct => p
        }
        .map(s => s.asJson)
      val result: Stream[F, Json] = ps
      Ok(result)
    }


    case GET -> Root / "last-id" =>
      val maxCurrentId = repo.getLastIdUsed
        .collect {
          case id: Int => id
        }
        .map(id => id.asJson)
      val result: Stream[F, Json] = maxCurrentId
      Ok(result)


    // Add an iem to the products table
    case req@POST -> Root / "product" => {
      req
        .as[MusicProduct]
        .flatMap { p =>
          for {
            count <- repo.addProduct(p)
            res <- count match {
              case 0 => NotFound()
              case _ => NoContent()
            }
          } yield res
        }
        .handleErrorWith {
          case InvalidMessageBodyFailure(dets, _) => BadRequest(s"Error details: $dets")
        }
    }

  }


}

