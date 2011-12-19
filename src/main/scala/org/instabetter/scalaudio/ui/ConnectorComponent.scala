package org.instabetter.scalaudio.ui

import scala.swing.{Component => SwingComponent}
import java.awt.Dimension
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Stroke
import java.awt.BasicStroke
import java.awt.RenderingHints
import scala.swing.event.ComponentResized
import scala.swing.event.UIElementResized

class ConnectorComponent(val parent:SwingComponent, 
        val from:IOPortComponent,val to:IOPortComponent) extends SwingComponent{
	
    listenTo(parent)
    
    reactions += {
    	case e:UIElementResized =>
    	    if(e.source == parent){
    	        this.preferredSize = parent.size
    	    }
    }
    
    override def paintComponent(g:Graphics2D){
        g.setColor(Color.BLACK)
        //g.setStroke(new BasicStroke(3f))
        
        val fromPos = from.getConnectionPoint()
        val toPos = to.getConnectionPoint()
        g.drawLine(fromPos._1, fromPos._2, toPos._1, toPos._2)
    }
}