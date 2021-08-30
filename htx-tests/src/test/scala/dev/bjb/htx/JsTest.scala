package dev.bjb.htx

import org.graalvm.polyglot.*

class JsTest extends CommonSuite:
  test("js") {
    val polyglot = Context.create()
    val array = polyglot.eval("js", "[1,2,42,4]")
    val result = array.getArrayElement(2).asInt
    println(array)
    assertEquals(result, 42)
  }
