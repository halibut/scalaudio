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
import scala.swing.{Component => SwingComponent}
import org.instabetter.scalaudio.components.controls.FloatControl
import org.instabetter.scalaudio.components.ComponentPort
import org.instabetter.scalaudio.components.ConnectableFrom




class ConfigurationCanvas(val config:AudioConfiguration, val configMeta:ConfigurationUIMetadata) extends ScrollableScalablePanel{
    
    scalablePanel.setDrawFunc(paintCanvas)
    
    private var _compUIMap:Map[Component,ComponentUI] = Map()
    private var _selectedPort:Option[(IOPortComponent,(Int,Int))] = None
    private var _otherSelectedPort:Option[(IOPortComponent,Boolean)] = None
    private var _hoverElement:Option[SwingComponent] = None
    
    
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
        for(portUI <- compUI.getPorts()){
            listenTo(portUI.mouse.moves)
            if(portUI.port.isInstanceOf[Connectable[_]])
            	listenTo(portUI.mouse.clicks)
        }
        listenTo(compUI.mouse.moves)
    }
    
    reactions += {
        case e:MousePressed =>
            if(e.source.isInstanceOf[IOPortComponent] && e.peer.getButton() == MouseEvent.BUTTON1){
                val portUI = e.source.asInstanceOf[IOPortComponent]
                val port = portUI.port
                if(port.isInstanceOf[Connectable[_]]){
                    val portPos = getPortPosition(port)
                    val mouseOffset = e.point
                    val endPoint = (portPos._1 + mouseOffset.getX, portPos._2 + mouseOffset.getY)
                	_selectedPort = Some((portUI,endPoint))
                	_prevMouseDragPos = e.point
                }
            }
        case e:MouseDragged =>
            if(_selectedPort.isDefined && e.source.isInstanceOf[IOPortComponent]){
                val newMousePos = e.point
                val diff = (newMousePos.getX - _prevMouseDragPos.getX,
                            newMousePos.getY - _prevMouseDragPos.getY)
                
                _prevMouseDragPos = e.point
                _selectedPort = _selectedPort.map{ case(portUI,(x,y)) =>
                    val endPoint = (x + diff._1, y + diff._2)
                    val port = portUI.port
                    if(port.isInstanceOf[ConnectableFrom[_,_]] && port.asInstanceOf[ConnectableFrom[_,_]].isConnected()){
                        val connectableFrom = port.asInstanceOf[ConnectableFrom[_,_]]
                        val outputPort = connectableFrom.getConnectedFrom().get 
                        connectableFrom.disconnect()
                        val (compUI,outputPortUI) = getComponentAndPortUIs(outputPort)
                        (outputPortUI,endPoint)
                    }
                    else{
                    	(portUI,endPoint)
                    }
                }
                if(_selectedPort.get._1.port.isInstanceOf[ConnectableFrom[_,_]]){
                    _selectedPort.get._1.port.asInstanceOf[ConnectableFrom[_,_]].disconnect()
                }
                repaint()
            }
            _otherSelectedPort.foreach{selectedPort =>
                if(!selectedPort._2){
                    _otherSelectedPort = None
                }
            }
        case e:MouseEntered =>
            if(_selectedPort.isDefined && _selectedPort.get._1 != e.source
                    && e.source.isInstanceOf[IOPortComponent]){
                val portUI = e.source.asInstanceOf[IOPortComponent]
                _otherSelectedPort = Some(portUI,true)
            }
            val hoverTextOpt:Option[(HoverText,(Int,Int))] = e.source match {
                case compUI:ComponentUI =>
                    val compUILoc = compUI.location
                    val labelText = Seq(
                            "Name: "+compUI.component.name,
                            "Desc: "+compUI.component.description)
                    Some((new HoverText(labelText),(compUILoc)))
                case portUI:IOPortComponent =>
                    val port = portUI.port
                    val portUILoc = getPortPosition(port)
                    var labelText = Seq(
                            "Name: "+port.name,
                            "Desc: "+port.description)
                    if(port.isInstanceOf[Control])
                        labelText :+= "Value: " + port.asInstanceOf[Control].controlValueText()
                    Some((new HoverText(labelText),portUILoc))
                case _ => 
                    None
            }
            hoverTextOpt.foreach{case(hoverText,(posX,posY)) =>
                _hoverElement = Some(hoverText)
            }
        case e:MouseExited =>
            _otherSelectedPort = _otherSelectedPort.map{case (selectedPort, over) =>
                (selectedPort, false)
            }
            _hoverElement.foreach{elem =>
                scalablePanel.remove(elem)
                _hoverElement = None
            }
        case e:MouseReleased =>
            if(_selectedPort.isDefined && e.source.isInstanceOf[IOPortComponent]){
                for(firstPort <- _selectedPort.map(_._1);
                		secondPort <- _otherSelectedPort){
                    if(secondPort._1.port.isInstanceOf[Connectable[_]]){
                        connectPorts(firstPort,secondPort._1)
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
            val from = getPortConnectorPosition(connect.port)
            
            g.drawLine(from._1,from._2,to._1,to._2)
        }
    }
    
    private def getPortPosition(connector:ComponentPort):(Int,Int) = {
        val (compUI,portUI) = getComponentAndPortUIs(connector)
        
        val compPos = compUI.location
        val portPos = portUI.location
        
        val x = (compPos.getX() + portPos.getX())
        val y = (compPos.getY() + portPos.getY())
        convertDoubleTupleToIntTuple(x,y)
    }
    
    private def getPortConnectorPosition(connector:ComponentPort):(Int,Int) = {
        val (compUI,portUI) = getComponentAndPortUIs(connector)
        
        val portPos = getPortPosition(connector)
        val portConnPos = portUI.getConnectionPoint()
        
        val x = (portPos._1 + portConnPos._1)
        val y = (portPos._2 + portConnPos._2)
        convertDoubleTupleToIntTuple(x,y)
    }
    
    private def getComponentAndPortUIs(port:ComponentPort):(ComponentUI,IOPortComponent) = {
        val comp = port.portOwner
        val compUI = _compUIMap(comp)
        val portUI = if(port.isInstanceOf[OutputSignal]){
            compUI.getPort(port.asInstanceOf[OutputSignal])
        } else if(port.isInstanceOf[InputSignal]){
            compUI.getPort(port.asInstanceOf[InputSignal])
        } else {
            compUI.getPort(port.asInstanceOf[Control])
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
    
    implicit private def convertPointToTuple2(point:Point):(Int,Int) = {
        (point.getX().asInstanceOf[Int],point.getY().asInstanceOf[Int])
    }
    implicit private def convertFloatTupleToIntTuple(fTuple:(Float,Float)):(Int,Int) = {
        (fTuple._1.asInstanceOf[Int],fTuple._2.asInstanceOf[Int])
    }
    implicit private def convertDoubleTupleToIntTuple(dTuple:(Double,Double)):(Int,Int) = {
        (dTuple._1.asInstanceOf[Int],dTuple._2.asInstanceOf[Int])
    }
}