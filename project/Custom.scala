import scala.util.Properties

sealed trait OS
case object Windows extends OS
case object Linux extends OS
case object MacOS extends OS

object OS {
  def get: OS = {
    if (Properties.isWin) Windows
    else if (Properties.isLinux) Linux
    else if (Properties.isMac) MacOS
    else throw new RuntimeException("Unsupported OS: " + Properties.osName)
  }
}
