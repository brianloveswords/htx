package dev.bjb.htx

import cats.effect.IO

class SelectorExtractorTest extends CommonSuite:
  test("title from html") {
    val ex = SelectorExtractor("{title}")
    ex.eval[IO]("<title>cool</title>") map { result =>
      assertEquals(result, List("cool"))
    }
  }
  test("multiple matching elements") {
    val html = """
    |<title>site</title>
    |<h2 class=author>A1</h2>
    |<h2 class=author>A2</h2>
    """.stripMargin

    val ex = SelectorExtractor("{title}: {.author}")
    ex.eval[IO](html) map { result =>
      assertEquals(result, List("site: A1", "site: A2"))
    }
  }
