package vkminer.util

trait ProgressBar {
  def progressBar(i: Int, max: Int, label: String = "", size: Int = 50, spacing: Int = 20) {
    val ratio  = i.toDouble / max
    val filled = (size * ratio).toInt
    val empty  = (size - filled).toInt
    val spaces = " " * (spacing - label.size)

    val bar = s"$label:$spaces[${"#" * filled}${" " * empty}] ${(ratio * 100).toInt}% $i/$max"
    print(s"\r\033[K\r$bar")
  }

  def withProgressBar[T](i: Int, max: Int, label: String = "")(task: => T): T = {
    progressBar(i, max, label)
    val res = task
    progressBar(i + 1, max, label)
    res
  }  
}