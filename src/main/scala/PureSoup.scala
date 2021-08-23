package mdlink

import cats.implicits.*
import org.jsoup.Jsoup
import scala.jdk.CollectionConverters.*

case class Element(name: String, attrs: Map[String, String], text: String)

type ExtractResult = Either[String, Option[Element]]

case class PureSoup(html: String):
  lazy val doc = Jsoup.parse(html)

  private def safeOption[T](a: => T): Option[T] =
    if a == null then None else Some(a)

  private def safeEither[T](a: => T): Either[Throwable, T] =
    try Right(a)
    catch { case e: Throwable => Left(e) }

  def extract(selector: String): Either[Throwable, Option[Element]] =
    safeEither(doc.selectFirst(selector)) map { element =>
      safeOption(element) map { el =>
        val name = el.nodeName.toLowerCase
        val attrs = el.attributes.asScala.map { attr =>
          attr.getKey -> attr.getValue
        }.toMap
        val text = el.text
        Element(name, attrs, text)
      }
    }
