package main

import cats.effect.*
import cats.effect.std.Console
import cats.effect.std.Semaphore
import cats.implicits.*

import scala.collection.immutable.Queue
import scala.concurrent.duration.*
import scala.util.Random

case class ServiceConfig(
    maxConcurrent: Int,
    maxResponseTime: Duration,
    maxErrorRate: Double,
    maxErrorResponseTime: FiniteDuration,
)

object Main extends IOApp:
  enum CapacityType:
    case Int
    case Double

  type CapacityMap = Map[CapacityType, Semaphore[IO]]

  def const[A](a: A)(_x: Any) = a

  def riskyBusiness[A](name: String)(op: => IO[A]): IO[A] = IO.defer {
    val risky =
      if Random.nextBoolean then
        IO.println(s"$name: successfully completed some risky business")
      else
        IO.raiseError(
          new RuntimeException(s"$name: failed to complete some risky business"),
        )
    IO.println(s"$name: starting risky business") *> risky.delayBy(1.second)
  } *> op

  def mkResource(name: String): Resource[IO, String] =
    val acquire =
      IO.println(s"Acquiring $name")
        >> riskyBusiness(name) { IO.pure(name) <* IO.println(s"Got $name") }

    val release = IO.println(s"Releasing $name").delayBy(1.second)

    Resource.make(acquire)(const(release))

  def usingTemps[A](f: (String, String) => IO[A]): IO[A] =
    (mkResource("temp1"), mkResource("temp2")).parTupled.use(f(_, _))

  class RandomService(cap: CapacityMap):
    def sleep(i: Int): IO[Unit] = IO.sleep(i.millis)

    val randSleep = IO(Random.nextInt(1000)) >>= sleep

    def randomInt(id: Int, max: Int): IO[(Int, Int)] =
      val result = randSleep *> IO(id -> Random.nextInt(max))
      withCapacity(CapacityType.Int)(result)

    def randomDouble(id: Int): IO[(Int, Double)] =
      val result = randSleep *> IO(id -> Random.nextDouble)
      withCapacity(CapacityType.Double)(result)

    private def withCapacity[A](capType: CapacityType)(f: => IO[A]): IO[A] =
      cap.get(capType).map(_.permit.use(const(f))).getOrElse(f)

  object RandomService:
    def apply(caps: (CapacityType, Int)*): IO[RandomService] = for
      entries <- caps.traverse { (t, c) => Semaphore[IO](c).map(s => t -> s) }
      map = Map(entries*)
    yield new RandomService(map)

  def run(args: List[String]): IO[ExitCode] =
    for
      rnd <- RandomService(
        CapacityType.Int -> 1,
        CapacityType.Double -> 2,
      )
      printRandomInt = rnd.randomInt(_: Int, 10) >>= { r =>
        Console[IO].println(s"Random int: $r")
      }
      printRandomDouble = rnd.randomDouble(_: Int) >>= { r =>
        Console[IO].println(s"Random double: $r")
      }
      _ <- usingTemps { (t1, t2) =>
        for
          _ <- riskyBusiness("main body of work") {
            List(
              (1 to 10).toList.map(printRandomInt).parSequence,
              (1 to 10).toList.map(printRandomDouble).parSequence,
            ).parSequence
          }
        yield ()
      }
    yield ExitCode.Success
