package org.instabetter.scalaudio.ui

import scala.swing.{Component => SwingComponent}
import java.awt.Dimension
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Stroke
import java.awt.BasicStroke

sealed abstract class IOPortLineSide
case object LeftSidePort extends IOPortLineSide
case object RightSidePort extends IOPortLineSide
case object TopSidePort extends IOPortLineSide
case object BottomSidePort extends IOPortLineSide

class IOPortComponent(color:Color, side:IOPortLineSide) extends SwingComponent{

    preferredSize = new Dimension(15, 15)
    
    override def paintComponent(g:Graphics2D){
        super.paintComponent(g)
        
        val compSize = this.size
        val rad = (math.min(compSize.getWidth(), compSize.getHeight()) * 0.3f).asInstanceOf[Float]
        val midX = (compSize.getWidth() / 2.0).asInstanceOf[Int]
        val midY = (compSize.getHeight() / 2.0).asInstanceOf[Int]
        
        g.setColor(color)
        g.fillOval(
                (midX-rad).asInstanceOf[Int], 
                (midY-rad).asInstanceOf[Int], 
                (rad * 2).asInstanceOf[Int], 
                (rad * 2).asInstanceOf[Int])
        
        val rWidth = (rad * .4).asInstanceOf[Int]
        side match{
            case LeftSidePort =>
                g.fillRect(0, midY-rWidth, midX, rWidth * 2)
            case RightSidePort =>
                g.fillRect(midX, midY-rWidth, midX, rWidth * 2)
            case TopSidePort =>
                g.fillRect(midX-rWidth, 0, rWidth * 2, midY)
            case BottomSidePort =>
                g.fillRect(midX-rWidth, midY, rWidth * 2, midY)
        }
        
    }
}