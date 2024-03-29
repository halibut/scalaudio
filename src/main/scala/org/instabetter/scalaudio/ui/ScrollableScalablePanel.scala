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
import scala.swing.event.MouseMoved


class ScrollableScalablePanel extends ScrollPane{
    val scalablePanel = new ScalablePanel()
    contents = scalablePanel
    this.peer.setWheelScrollingEnabled(false)
    
    private var dragging = false
    private var mousePressPos:Point = null
    
    
    def add(comp:SwingComponent, x:Int, y:Int, moveable:Boolean = false){
        scalablePanel.add(comp, new PositionConstraint(x,y))
        scalablePanel.revalidate()
        
        if(moveable){
            listenTo(comp.mouse.clicks, comp.mouse.moves)
            
	        reactions += {
                case e:MousePressed => {
	                if(e.source == comp && e.peer.getButton() == MouseEvent.BUTTON1){
	                    mousePressPos = e.point
	                    dragging = true
	                }
	            }
	            case e:MouseReleased => {
				    if(e.source == comp && e.peer.getButton() == MouseEvent.BUTTON1){
				        dragging = false
				    }
				}
				case e:MouseDragged => {
				    if(e.source == comp && dragging){
				        val xDrag = e.point.getX() - mousePressPos.getX()
				        val yDrag = e.point.getY() - mousePressPos.getY()
				        val zoom = scalablePanel.getZoom()
				        val pos = scalablePanel.constraintsFor(comp)
				        val newPosX = pos.x + (xDrag / zoom).asInstanceOf[Int]
				        val newPosY = pos.y + (yDrag / zoom).asInstanceOf[Int]
				        scalablePanel.moveComponent(comp, new PositionConstraint(newPosX, newPosY))
				        scalablePanel.revalidate()
				        repaint()
				        if(newPosX < 0 || newPosY < 0){
				            Thread.sleep(100)
				        }
				    }
				}
	        }
        }
    }
    
    listenTo(mouse.clicks, mouse.wheel, mouse.moves)
    
    reactions += {
		case e:MouseWheelMoved => {
		    if(e.source == this){
		        val zoom = -e.rotation * 0.1f
		        scalablePanel.setZoom(scalablePanel.getZoom() + zoom)
		        scalablePanel.revalidate()
		        repaint()
		    }
		}
		case e:MousePressed => {
		    if(e.source == this && e.peer.getButton() == MouseEvent.BUTTON1){
		        mousePressPos = e.point
		        dragging = true
		    }
		}
		case e:MouseReleased => {
		    if(e.source == this && e.peer.getButton() == MouseEvent.BUTTON1){
		        dragging = false
		    }
		}
		case e:MouseDragged => {
		    if(e.source == this && dragging){
		        val xDrag = e.point.getX() - mousePressPos.getX()
		        val yDrag = e.point.getY() - mousePressPos.getY()
		        horizontalScrollBar.value -= xDrag.asInstanceOf[Int]
		        verticalScrollBar.value -= yDrag.asInstanceOf[Int]
		        mousePressPos = e.point
		    }
		}
	}

    def setScrollPosition(x:Int,y:Int){
        horizontalScrollBar.value = x
        verticalScrollBar.value = y
    }
    
    def getScrollPosition():(Int,Int) = {
        (horizontalScrollBar.value, verticalScrollBar.value)
    }
    
}

