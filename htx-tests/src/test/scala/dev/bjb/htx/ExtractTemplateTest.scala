package dev.bjb.htx

import scala.util.control.NoStackTrace

// htx example.com "[{h1.title}]({@})"
// htx example.com author:"meta[property='twitter:title']" "[{h1.title}]({@})"

enum ExtractTemplateError extends NoStackTrace:
  case EmptyTemplate

case class ExtractTemplate private (
    extractors: Seq[Extract],
    template: Template,
)
case object ExtractTemplate:
  def from(
      extractors: Seq[Extract],
      template: Template,
  ): Either[ExtractTemplateError, ExtractTemplate] =
    Left(ExtractTemplateError.EmptyTemplate)

class ExtractTemplateTest extends CommonSuite:
  test("parse empty pair") {
    val result = ExtractTemplate.from(
      extractors = List(),
      template = Template("cool"),
    )
    assertEquals(result, Left(ExtractTemplateError.EmptyTemplate))
  }
