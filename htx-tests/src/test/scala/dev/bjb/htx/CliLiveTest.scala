package dev.bjb.htx

import cli.Cli
import cats.implicits.*
import cats.effect.ExitCode
import cats.effect.std.Console
import cats.effect.IO
import cats.effect.Ref
import cats.Show
import java.nio.charset.Charset
import java.io.IOException

case class Stdio(out: String, err: String, in: List[String])
object Stdio:
  def empty: Stdio = Stdio("", "", List.empty)

trait MockConsole extends Console[IO]:
  def getStdio: IO[Stdio]

def mkConsole: IO[MockConsole] =
  for stdio <- Ref.of[IO, Stdio](Stdio.empty)
  yield new MockConsole {
    def error[A](a: A)(using Show[A]): IO[Unit] = stdio.update {
      case Stdio(out, err, in) => Stdio(out, err + a, in)
    }
    def errorln[A](a: A)(using Show[A]): IO[Unit] = error(a.show + "\n")
    def print[A](a: A)(using Show[A]): IO[Unit] = stdio.update {
      case Stdio(out, err, in) => Stdio(out + a, err, in)
    }
    def println[A](a: A)(using Show[A]): IO[Unit] = print(a.show + "\n")
    def readLineWithCharset(charset: Charset): IO[String] =
      IO.raiseError(IOException("readLineWithCharset not implemented"))
    def getStdio = stdio.get
  }

class CliLiveTest extends CommonSuite:
  test("example: mdlink template") {
    for
      console <- mkConsole
      testCli = new Cli[IO](using console) {}
      result <- testCli.run(List("https://example.com", "[{title}]({@})"))
      stdio <- console.getStdio
    yield
      assertEquals(stdio.out, "[Example Domain](https://example.com)")
      assertEquals(result, ExitCode.Success)
  }

  test("example: alternate template") {
    for
      console <- mkConsole
      testCli = new Cli[IO](using console) {}
      result <- testCli.run(List("https://example.com", "**{title}**: {@}"))
      stdio <- console.getStdio
    yield
      assertEquals(stdio.out, "**Example Domain**: https://example.com")
      assertEquals(result, ExitCode.Success)
  }
