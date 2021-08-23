package mdlink

import org.scalacheck.Gen

def safeOption[T](a: => T): Option[T] =
  if a == null then None else Some(a)

def safeEither[T](a: => T): Either[Throwable, T] =
  try Right(a)
  catch { case e: Throwable => Left(e) }

val genLowerNonEmpty = Gen.alphaLowerStr.suchThat(_.nonEmpty)
