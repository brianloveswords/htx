package main
import cats.effect.*

object MdLink extends IOApp:
  def run(args: List[String]) =
    IO.println("ok").as(ExitCode.Success)
