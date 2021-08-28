package dev.bjb.htx.cli

import org.http4s.Uri

case class CliConfigRaw(
    num: Option[Int] = None,
    uri: String,
    kwargs: Map[String, String] = Map.empty,
)
