package dev.bjb.htx

import scala.util.control.NoStackTrace

// htx example.com "[{h1.title}]({@})"
// htx example.com author:"meta[property='twitter:title']" "[{h1.title}]({@})"

enum ExtractTemplateError extends NoStackTrace:
  case EmptyTemplate

case class ExtractTemplate(aliasList: List[String], template: String)
case object ExtractTemplate:
  def apply(
      aliasList: List[String],
      template: String,
  ): Either[ExtractTemplateError, ExtractTemplate] =
    Left(ExtractTemplateError.EmptyTemplate)

class ExtractTemplateTest extends CommonSuite:
  test("parse empty pair") {
    val result = ExtractTemplate(
      aliasList = List(),
      template = "",
    )
    assertEquals(result, Left(ExtractTemplateError.EmptyTemplate))
  }
