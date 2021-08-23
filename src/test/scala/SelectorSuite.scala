package mdlink

import io.circe.syntax.*
import io.circe.parser.*
import io.circe.generic.auto.*
import io.circe.DecodingFailure

class SelectorSuite extends RoundTripSuite[Selector]:
  roundtrip

  test("example: bad decoder") {
    decode[Selector](""""~!~"""") match
      case Left(DecodingFailure((message, _))) =>
        assert(
          message.contains(
            "Could not parse query '!': unexpected token at '!'",
          ),
          s"unexpected message: $message",
        )
      case r => fail(s"expected failure, got $r")
  }
