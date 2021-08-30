package dev.bjb.htx

import cats.effect.IO

class SelectorExtractorTest extends CommonSuite:
  def unsafeExtractor(s: String): SelectorExtractor[IO] =
    SelectorExtractor[IO](TemplateEvaluator.unsafe(s))

  test("title from html") {
    val ex = unsafeExtractor("{title}")
    ex.eval("<title>cool</title>") map { result =>
      assertEquals(result, List("cool"))
    }
  }

  test("title from html, but shouted") {
    val ex = unsafeExtractor("{title |> trim |> upper}")
    ex.eval("<title>    cool  </title>") map { result =>
      assertEquals(result, List("COOL"))
    }
  }

  test("title from html, but shouted using JS") {
    val ex = unsafeExtractor("{title |> upper.js |> trim}")
    ex.eval("<title>    cool  </title>") map { result =>
      assertEquals(result, List("COOL"))
    }
  }

  test("multiple matching elements") {
    val html = """
    |<title>site</title>
    |<h2 class=author>A1</h2>
    |<h2 class=author>A2</h2>
    """.stripMargin

    val ex = unsafeExtractor("{title}: {.author}")
    ex.eval(html) map { result =>
      assertEquals(result, List("site: A1", "site: A2"))
    }
  }
