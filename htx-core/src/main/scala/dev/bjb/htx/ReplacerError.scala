package dev.bjb.htx

import cats.Eq
import cats.implicits.*
import scala.util.control.NoStackTrace

enum ReplacerError extends NoStackTrace:
  case NoReplacements(template: Template)
  case UnfulfilledAutoUri(template: Template)
  case UnusedExtracts(extractors: ExtractorMap)
  case InvalidSelectorFromTemplate(
      template: Template,
      selector: String,
      reason: String,
  )

object ReplacerError:
  given Eq[ReplacerError] =
    Eq.fromUniversalEquals[ReplacerError]
