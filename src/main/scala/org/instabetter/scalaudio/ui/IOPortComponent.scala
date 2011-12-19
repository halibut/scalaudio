package org.instabetter.scalaudio.ui

import scala.swing.{Component => SwingComponent}
import java.awt.Dimension
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Stroke
import java.awt.BasicStroke
import java.awt.RenderingHints
import org.instabetter.scalaudio.components.Connectable
import org.instabetter.scalaudio.components.ComponentPort

sealed abstract class IOPortLineSide
case object LeftSidePort extends IOPortLineSide
case object RightSidePort extends IOPortLineSide
case object TopSidePort extends IOPortLineSide
case object BottomSidePort extends IOPortLineSide
case object NoConnectionPort extends IOPortLineSide

class IOPortComponent(val port:ComponentPort, val parent:ComponentUI, val color:Color, val side:IOPortLineSide) extends SwingComponent{
    preferredSize = new Dimension(15, 15)
	
	val unconnectedColor:Color = {
	    val grayColors = Array[Float](.7f,.7f,.7f)
        val colors = color.getColorComponents(null)
        val newColor = grayColors.zip(colors).map{case (gray, orig) =>
            val diff = gray - orig
            val newVal = orig + diff * .75f
            newVal
        }
	    new Color(newColor(0),newColor(1),newColor(2))
	}
    
    override def paintComponent(g:Graphics2D){
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        
        super.paintComponent(g)
        
        val compSize = this.size
        val rad = (math.min(compSize.getWidth(), compSize.getHeight()) * 0.3f).asInstanceOf[Float]
        val midX = (compSize.getWidth() / 2.0).asInstanceOf[Int]
        val midY = (compSize.getHeight() / 2.0).asInstanceOf[Int]
        
        if(port.isInstanceOf[Connectable[_]] && !port.asInstanceOf[Connectable[_]].isConnected()){
            g.setColor(unconnectedColor)   
        }
        else{
            g.setColor(color)
        }
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
            case NoConnectionPort => { /*Do Nothing*/}
        }
    }

	def getConnectionPoint():(Int,Int) = {
	    val compSize = this.size
	    val left = 0
	    val right = compSize.getWidth().asInstanceOf[Int]
	    val top = 0
	    val bottom = compSize.getHeight().asInstanceOf[Int]
        val midX = (right / 2.0).asInstanceOf[Int]
        val midY = (bottom / 2.0).asInstanceOf[Int]
	    
	    side match{
            case LeftSidePort =>
                (left, midY)
            case RightSidePort =>
                (right, midY)
            case TopSidePort =>
                (midX, top)
            case BottomSidePort =>
                (midX, bottom)
            case NoConnectionPort =>
                (midX, midY)
        }
	}
	
	
}