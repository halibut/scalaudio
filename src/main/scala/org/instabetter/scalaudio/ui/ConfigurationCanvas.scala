package org.instabetter.scalaudio
package ui
import org.instabetter.scalaudio.components.ComponentOutputs
import org.instabetter.scalaudio.components.InputSignal
import org.instabetter.scalaudio.components.Component
import org.instabetter.scalaudio.components.controls.Control
import java.awt.Graphics2D
import java.awt.Color
import java.awt.BasicStroke
import java.awt.RenderingHints
import org.instabetter.scalaudio.components.Connectable
import org.instabetter.scalaudio.components.OutputSignal
import scala.swing.event.MousePressed
import java.awt.event.MouseEvent
import java.awt.Point
import scala.swing.event.MouseDragged
import scala.swing.event.MouseReleased
import scala.swing.event.MouseEntered
import scala.swing.event.MouseExited
import org.instabetter.scalaudio.components.controls.FloatControl




class ConfigurationCanvas(val config:AudioConfiguration, val configMeta:ConfigurationUIMetadata) extends ScrollableScalablePanel{
    
    scalablePanel.setDrawFunc(paintCanvas)
    
    private var _compUIMap:Map[Component,ComponentUI] = Map()
    private var _selectedPort:Option[(IOPortComponent,(Int,Int))] = None
    private var _otherSelectedPort:Option[IOPortComponent] = None
    
    private var _prevMouseDragPos:Point = null
    
    {
        var curX = 20
        var curY = 20
	    for(comp <- config.getComponents()){
	        val compUI = new ComponentUI(comp)
	        val compPos = configMeta.getComponentPosition(comp).getOrElse{
	            val pos = (curX,curY)
	            curX += 120
	            if(curX >= 600){
	                curX = 20
	                curY += 100
	            }
	            if(curY >= 600){
	                curY = 20
	        	}
	            
	            pos
	        }
	        addCompUI(compUI, compPos._1, compPos._2)
	    }
    }
    
    def addCompUI(compUI:ComponentUI, x:Int, y:Int){
        this.add(compUI,x,y,true)
        _compUIMap += (compUI.component -> compUI)
        for(port <- compUI.getConnectablePorts()){
            listenTo(port.mouse.clicks, port.mouse.moves)
        }
    }
    
    reactions += {
        case e:MousePressed =>
            if(e.source.isInstanceOf[IOPortComponent] && e.peer.getButton() == MouseEvent.BUTTON1){
                val portUI = e.source.asInstanceOf[IOPortComponent]
                val port = portUI.port
                if(port.isInstanceOf[Connectable[_]]){
                    val connectablePort = port.asInstanceOf[Connectable[_]]
                    val portPos = getPortPosition(connectablePort)
                    val mouseOffset = e.point
                    val endPointX = portPos._1 + mouseOffset.getX.asInstanceOf[Int]
                    val endPointY = portPos._2 + mouseOffset.getY.asInstanceOf[Int]
                	_selectedPort = Some((portUI,(endPointX,endPointY)))
                	_prevMouseDragPos = e.point
                }
            }
        case e:MouseDragged =>
            if(_selectedPort.isDefined && _selectedPort.get._1 == e.source){
                val newMousePos = e.point
                val diffX = (newMousePos.getX - _prevMouseDragPos.getX).asInstanceOf[Int]
                val diffY = (newMousePos.getY - _prevMouseDragPos.getY).asInstanceOf[Int]
                
                _prevMouseDragPos = e.point
                _selectedPort = _selectedPort.map{ case(port,(x,y)) =>
                    (port,(x + diffX, y + diffY))
                }
                repaint()
            }
        case e:MouseEntered =>
            if(_selectedPort.isDefined && _selectedPort.get._1 != e.source
                    && e.source.isInstanceOf[IOPortComponent]){
                val portUI = e.source.asInstanceOf[IOPortComponent]
                _otherSelectedPort = Some(portUI)
            }
        case e:MouseReleased =>
            if(_selectedPort.isDefined && _selectedPort.get._1 == e.source){
                for(firstPort <- _selectedPort.map(_._1);
                		secondPort <- _otherSelectedPort){
                    if(secondPort.port.isInstanceOf[Connectable[_]]){
                        connectPorts(firstPort,secondPort)
                    }
                }
                _selectedPort = None
                _otherSelectedPort = None
                repaint()
            }
    }
    
    def paintCanvas(g:Graphics2D){
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.setColor(Color.GRAY)
        g.setStroke(new BasicStroke(2f))
        
        config.getConnections().foreach{case (out,in)=>
            val (outX,outY) = getPortConnectorPosition(out)
            val (inX,inY) = getPortConnectorPosition(in)
            
            g.drawLine(outX, outY, inX, inY)
        }
        
        _selectedPort.foreach{case (connect,to) =>
            val from = getPortConnectorPosition(connect.port.asInstanceOf[Connectable[_]])
            g.drawLine(from._1,from._2,to._1,to._2)
        }
    }
    
    private def getPortPosition(connector:Connectable[_]):(Int,Int) = {
        val (compUI,portUI) = getComponentAndPortUIs(connector)
        
        val compPos = compUI.location
        val portPos = portUI.location
        
        val x = (compPos.getX() + portPos.getX()).asInstanceOf[Int]
        val y = (compPos.getY() + portPos.getY()).asInstanceOf[Int]
        (x,y)
    }
    
    private def getPortConnectorPosition(connector:Connectable[_]):(Int,Int) = {
        val (compUI,portUI) = getComponentAndPortUIs(connector)
        
        val portPos = getPortPosition(connector)
        val portConnPos = portUI.getConnectionPoint()
        
        val x = (portPos._1 + portConnPos._1).asInstanceOf[Int]
        val y = (portPos._2 + portConnPos._2).asInstanceOf[Int]
        (x,y)
    }
    
    private def getComponentAndPortUIs(connector:Connectable[_]):(ComponentUI,IOPortComponent) = {
        val comp = connector.connectionOwner
        val compUI = _compUIMap(comp)
        val portUI = if(connector.isInstanceOf[OutputSignal]){
            compUI.getPort(connector.asInstanceOf[OutputSignal])
        } else if(connector.isInstanceOf[InputSignal]){
            compUI.getPort(connector.asInstanceOf[InputSignal])
        } else {
            compUI.getPort(connector.asInstanceOf[Control])
        }
        (compUI,portUI)
    }
    
    private def connectPorts(first:IOPortComponent,second:IOPortComponent){
        val output:OutputSignal = 
            if(first.port.isInstanceOf[OutputSignal])
                first.port.asInstanceOf[OutputSignal]
            else if(second.port.isInstanceOf[OutputSignal])
                second.port.asInstanceOf[OutputSignal]
            else
                return
                
        val control:FloatControl = 
            if(first.port.isInstanceOf[FloatControl])
                first.port.asInstanceOf[FloatControl]
            else if(second.port.isInstanceOf[FloatControl])
                second.port.asInstanceOf[FloatControl]
            else null
        
        val input:InputSignal = 
            if(first.port.isInstanceOf[InputSignal])
                first.port.asInstanceOf[InputSignal]
            else if(second.port.isInstanceOf[InputSignal])
                second.port.asInstanceOf[InputSignal]
            else null
        
        if(input != null)
            output --> input
        else if(control != null)
            output --> control
        else
            return

    }
}