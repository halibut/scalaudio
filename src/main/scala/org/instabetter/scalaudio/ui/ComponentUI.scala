package org.instabetter.scalaudio
package ui

import components._
import scala.swing.{Component => SwingComponent}
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.RenderingHints
import scala.swing.Label
import scala.swing.Alignment
import java.awt.FontMetrics
import org.instabetter.scalaudio.components.controls.Control

class ComponentUI(val component:Component) extends ScalablePanel{
    private val NAME_HEIGHT = 16
    private val MIN_HEIGHT = 40
    private val MIN_WIDTH = 60
    private val CONTROL_COLOR = new Color(.5f,.5f,.5f)
    private val INPUT_COLOR = new Color(0f, 0f, .75f)
    private val OUTPUT_COLOR = new Color(0f, .75f, 0f)
    private val COMPONENT_COLOR = new Color(.8f, .8f, .8f)
    
    private var _inputPorts:Vector[IOPortComponent] = Vector()
    private var _outputPorts:Vector[IOPortComponent] = Vector()
    private var _controlPorts:Vector[IOPortComponent] = Vector()
    
    this.opaque = false
    
	recalcLayout()
	
	def recalcLayout(){
        removeAllComponents()
        
        _inputPorts = Vector()
        _outputPorts = Vector()
        _controlPorts = Vector()
        
	    val (controlHeight,numControls) = component match{
	        case controls:ComponentControls => (15, controls.controls.size)
	        case _ => (0,0)
	    }
	    val (inputWidth,numInputs) = component match{
	        case inputs:ComponentInputs => (15, inputs.inputs.size)
	        case _ => (0,0)
	    }
	    val (outputWidth,numOutputs) = component match{
	        case outputs:ComponentOutputs => (15, outputs.outputs.size)
	        case _ => (0,0)
	    }
	    
	    val nameLabel = new ScalableLabel(component.name, 12f)
	    nameLabel.horizontalAlignment = Alignment.Center
	    nameLabel.verticalAlignment = Alignment.Center
	    val compNameWidth = nameLabel.peer.getFontMetrics(nameLabel.font).stringWidth(component.name)
	    
	    val minWidth = math.min(math.max(MIN_WIDTH, 5 + compNameWidth), 80)
	    
	    val totalWidth = math.max(minWidth, 15 * numControls) + inputWidth + outputWidth
	    val totalHeight = math.max(math.max(MIN_HEIGHT, 15 * numInputs), 15 * numOutputs) + controlHeight
	    
	    nameLabel.preferredSize = new Dimension(totalWidth, NAME_HEIGHT)
	    add(nameLabel, PositionConstraint(0,0))
	    
	    val centerX = totalWidth / 2
	    val centerY = totalHeight / 2
	    
	    this.preferredSize = new Dimension(totalWidth, totalHeight + NAME_HEIGHT)
	    
	    val controlY =  + NAME_HEIGHT + totalHeight - controlHeight
	    val controlXStart = centerX - (15 * numControls)/2
	    for(i <- 0 until numControls){
	        val control = component.asInstanceOf[ComponentControls].controls(i)
	        val position = PositionConstraint(controlXStart + i * 15, controlY)
	        val connectDirection = 
	            if(control.isInstanceOf[Connectable[_]])
	            	BottomSidePort
	            else
	                NoConnectionPort
	        val port = new IOPortComponent(control, this, CONTROL_COLOR, connectDirection) 
	        add(port, position)
	        _controlPorts :+= port 
	    }
	    
	    val inputX = 0
	    val inputYStart = NAME_HEIGHT + centerY - (15 * numInputs)/2
	    for(i <- 0 until numInputs){
	        val input = component.asInstanceOf[ComponentInputs].inputs(i)
	        val position = PositionConstraint(inputX, inputYStart + i * 15)
	        val port = new IOPortComponent(input, this, INPUT_COLOR, LeftSidePort) 
	        add(port, position)
	        _inputPorts :+= port 
	    }
	    
	    val outputX = totalWidth - outputWidth
	    val outputYStart = NAME_HEIGHT + centerY - (15 * numOutputs)/2
	    for(i <- 0 until numOutputs){
	        val output = component.asInstanceOf[ComponentOutputs].outputs(i)
	        val position = PositionConstraint(outputX, outputYStart + i * 15)
	        val port = new IOPortComponent(output, this, OUTPUT_COLOR, RightSidePort)
	        add(port, position)
	        _outputPorts :+= port
	    }
	}
    
    override def paintComponent(g:Graphics2D){
        super.paintComponent(g)
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        
        val width = this.size.width.asInstanceOf[Int]
        val height = this.size.height.asInstanceOf[Int]
        
        val cornerDiameter = (getZoom() * 10).asInstanceOf[Int]
        
        g.setColor(Color.DARK_GRAY)
        g.fillRoundRect(0, 0, width, height, cornerDiameter, cornerDiameter)
        
        g.setColor(COMPONENT_COLOR)
        g.fillRoundRect(1, 1, width-2, height-2, cornerDiameter, cornerDiameter)
        
        val nameHeight = ((NAME_HEIGHT - 1) * getZoom()).asInstanceOf[Int]
        g.setColor(Color.DARK_GRAY)
        g.drawRect(1, nameHeight , width-2, 0)
    }
    
    def getPort(input:InputSignal):IOPortComponent = {
        val ind = component.asInstanceOf[ComponentInputs].inputs.indexOf(input)
        _inputPorts(ind)
    }
    def getPort(output:OutputSignal):IOPortComponent = {
        val ind = component.asInstanceOf[ComponentOutputs].outputs.indexOf(output)
        _outputPorts(ind)
    }
    def getPort(control:Control):IOPortComponent = {
        val ind = component.asInstanceOf[ComponentControls].controls.indexOf(control)
        _controlPorts(ind)
    }
    
    def getConnectablePorts():Seq[IOPortComponent] = {
        _inputPorts ++ _outputPorts ++ _controlPorts.filter(_.port.isInstanceOf[Connectable[_]])
    }
    
    
}