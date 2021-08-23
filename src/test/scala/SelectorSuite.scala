package mdlink

import io.circe.syntax.*
import io.circe.parser.*
import io.circe.generic.auto.*

class SelectorSuite extends RoundTripSuite[Selector]:
  import Selector.*

  roundtrip
