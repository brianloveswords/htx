package dev.bjb.htx

import io.circe.generic.auto.*

class TemplateTest extends RoundTripSuite[Template]:
  roundtrip
