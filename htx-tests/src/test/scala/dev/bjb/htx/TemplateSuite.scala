package dev.bjb.htx

import io.circe.generic.auto.*

class TemplateSuite extends RoundTripSuite[Template]:
  roundtrip
