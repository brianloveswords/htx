package dev.bjb.htx

import cats.implicits.*
import org.jsoup.Jsoup
import org.jsoup.select.QueryParser
import org.jsoup.select.Evaluator
import scala.jdk.CollectionConverters.*

case class Element(name: String, attrs: Map[String, String], text: String)

case class PureSoup(html: String):
  lazy val doc = Jsoup.parse(html)

  def extract(selector: Selector): List[Element] =
    val elements = doc.select(selector)
    elements.asScala.toList map { el =>
      val name = el.nodeName.toLowerCase
      val attrs = el.attributes.asScala.map { attr =>
        attr.getKey -> attr.getValue
      }.toMap
      val text = el.text
      Element(name, attrs, text)
    }

  override def toString: String = s"PureSoup(html = $html, doc = $doc)"
