package vkminer.util

import vkminer.strategies.generic.BasicStrategy

trait ProgressBar {this: BasicStrategy => 
  def progressBar(i: Int, max: Int, tag: String, label: String, size: Int = 50, spacing: Int = 20) {
    val ratio  = i.toDouble / max
    val filled = (size * ratio).toInt
    val empty  = (size - filled).toInt
    val spaces = " " * (spacing - label.size)

    val bar = s"$label:$spaces[${"#" * filled}${" " * empty}] ${(ratio * 100).toInt}% $i/$max"
    
    print(s"\r\033[K\r$bar")
  
    update(tag, i, max)
  }

  def withProgressBar[T](i: Int, max: Int, tag: String, label: String)(task: => T): T = {
    progressBar(i, max, tag, label)
    val res = task
    progressBar(i + 1, max, tag, label)
    res
  }  
}