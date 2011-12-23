package org.instabetter.scalaudio.ui

import scala.swing.Label

class HoverText(lines:Seq[String]) extends Label {
    
    this.text = lines.mkString("<html>","<br>","</html>")
}