package main

import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite

trait CommonSuite extends CatsEffectSuite with ScalaCheckEffectSuite
