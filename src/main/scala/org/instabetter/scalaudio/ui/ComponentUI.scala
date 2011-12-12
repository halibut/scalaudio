package org.instabetter.scalaudio
package ui

import components._
import scala.swing.{Component => SwingComponent}
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D

class ComponentUI(component:Component) extends ScalablePanel{
    private val MIN_HEIGHT = 40
    private val MIN_WIDTH = 40
    private val CONTROL_COLOR = new Color(.5f,.5f,.5f)
    private val INPUT_COLOR = new Color(0f, 0f, .75f)
    private val OUTPUT_COLOR = new Color(0f, .75f, 0f)
    private val COMPONENT_COLOR = new Color(.8f, .8f, .8f)
    
    this.opaque = false
    
	recalcLayout()
	
	def recalcLayout(){
        removeAllComponents()
        
	    val (controlHeight,numControls) = component match{
	        case controls:ComponentControls => (15, controls.controls().size)
	        case _ => (0,0)
	    }
	    val (inputWidth,numInputs) = component match{
	        case inputs:ComponentInputs => (15, inputs.inputs().size)
	        case _ => (0,0)
	    }
	    val (outputWidth,numOutputs) = component match{
	        case outputs:ComponentOutputs => (15, outputs.outputs().size)
	        case _ => (0,0)
	    }
	    
	    val totalWidth = math.max(MIN_WIDTH, 15 * numControls) + inputWidth + outputWidth
	    val totalHeight = math.max(math.max(MIN_HEIGHT, 15 * numInputs), 15 * numOutputs) + controlHeight
	    
	    val centerX = totalWidth / 2
	    val centerY = totalHeight / 2
	    
	    this.preferredSize = new Dimension(totalWidth, totalHeight)
	    
	    val controlY = totalHeight - controlHeight
	    val controlXStart = centerX - (15 * numControls)/2
	    for(i <- 0 until numControls){
	        val position = PositionConstraint(controlXStart + i * 15, controlY)
	        add(new IOPortComponent(CONTROL_COLOR, BottomSidePort), position)
	    }
	    
	    val inputX = 0
	    val inputYStart = centerY - (15 * numInputs)/2
	    for(i <- 0 until numInputs){
	        val position = PositionConstraint(inputX, inputYStart + i * 15)
	        add(new IOPortComponent(INPUT_COLOR, LeftSidePort), position)
	    }
	    
	    val outputX = totalWidth - outputWidth
	    val outputYStart = centerY - (15 * numOutputs)/2
	    for(i <- 0 until numOutputs){
	        val position = PositionConstraint(outputX, outputYStart + i * 15)
	        add(new IOPortComponent(OUTPUT_COLOR, RightSidePort), position)
	    }
	}
    
    override def paintComponent(g:Graphics2D){
        val width = this.size.width.asInstanceOf[Int]
        val height = this.size.height.asInstanceOf[Int]
        
        val cornerDiameter = (getZoom() * 10).asInstanceOf[Int]
        
        g.setColor(Color.GRAY)
        g.fillRoundRect(0, 0, width, height, cornerDiameter, cornerDiameter)
        
        g.setColor(COMPONENT_COLOR)
        g.fillRoundRect(1, 1, width-2, height-2, cornerDiameter, cornerDiameter)
    }
}