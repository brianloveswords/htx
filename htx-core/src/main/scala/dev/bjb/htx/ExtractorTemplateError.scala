package dev.bjb.htx

import cats.Eq
import cats.implicits.*
import scala.util.control.NoStackTrace

enum ExtractorTemplateError extends NoStackTrace:
  case NoReplacements(template: Template)
  case UnusedExtracts(extractors: ExtractorMap)
  case InvalidSelectorFromTemplate(
      template: Template,
      selector: String,
      reason: String,
  )

object ExtractorTemplateError:
  given Eq[ExtractorTemplateError] =
    Eq.fromUniversalEquals[ExtractorTemplateError]
