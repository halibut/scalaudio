package org.instabetter.scalaudio.ui

import scala.swing.{Component => SwingComponent, Panel}
import scala.swing.LayoutContainer
import java.awt.LayoutManager2
import scala.swing.Container
import java.awt.Dimension
import java.awt.{Component => AWTComponent}
import java.awt.{Container => AWTContainer}
import scala.swing.ScrollPane
import scala.swing.event.MouseWheelMoved
import scala.swing.ScrollBar
import scala.swing.event.MouseDragged
import java.awt.Point
import scala.swing.event.MousePressed
import java.awt.event.MouseEvent
import scala.swing.event.MouseReleased
import java.awt.Graphics2D



class ScalablePanel() extends Panel with LayoutContainer {
	def layoutManager = peer.getLayout.asInstanceOf[ScalableAbsPositionLayout]
	override lazy val peer = new javax.swing.JPanel(new ScalableAbsPositionLayout) with SuperMixin 
	
	type Constraints = PositionConstraint
	
	private var _components:Set[SwingComponent] = Set()
	
	private var _drawFunc:Option[(Graphics2D)=>Unit] = None
	
	def constraintsFor(comp: SwingComponent): Constraints =
		layoutManager.getConstraints(comp.peer)
	protected def areValid(c: Constraints): (Boolean, String) = (true, "")
	def add(c: SwingComponent, l: Constraints) {
		peer.add(c.peer, l)
		_components += c
	}
	
	def remove(c: SwingComponent) {
	    peer.remove(c.peer)
	    _components -= c
	}
	
	def removeAllComponents() {
	    peer.removeAll()
	    _components = Set()
	}
	
	def moveComponent(c:SwingComponent, l: Constraints) {
	    peer.add(c.peer, l)
	    _components += c
	}
    
    def setZoom(zoom:Float) { 
        layoutManager.setZoom(zoom); 
        val comps = _components
        	.filter(_.isInstanceOf[ScalablePanel])
        	.map(_.asInstanceOf[ScalablePanel])
        	.foreach{_.setZoom(zoom)}

        revalidate() 
    }
    def getZoom():Float = { layoutManager.getZoom() }
    
    def setDrawFunc(drawFunc:(Graphics2D)=>Unit){
        _drawFunc = Option(drawFunc)
    }
    
    override def paintComponent(g:Graphics2D){
        super.paintComponent(g)
        _drawFunc.foreach{_(g)}
    }
}
