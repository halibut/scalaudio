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


class ScrollableScalablePanel extends ScrollPane{
    val scalablePanel = new ScalablePanel()
    contents = scalablePanel
    
    private var prevMouseDragPos:Point = null
    private var dragging = false
    
    
    def add(comp:SwingComponent, x:Int, y:Int, moveable:Boolean = false){
        scalablePanel.add(comp, new PositionConstraint(x,y))
        scalablePanel.revalidate()
        
        if(moveable){
            listenTo(comp.mouse.clicks, comp.mouse.moves)
            
	        reactions += {
	            case e:MousePressed => {
	                if(e.source == comp && e.peer.getButton() == MouseEvent.BUTTON1){
	                    prevMouseDragPos = e.point
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
				        val xDrag = e.point.getX() - prevMouseDragPos.getX()
				        val yDrag = e.point.getY() - prevMouseDragPos.getY()
				        val zoom = scalablePanel.getZoom()
				        val pos = scalablePanel.constraintsFor(comp)
				        val newPosX = pos.x + (xDrag / zoom).asInstanceOf[Int]
				        val newPosY = pos.y + (yDrag / zoom).asInstanceOf[Int]
				        scalablePanel.moveComponent(comp, new PositionConstraint(newPosX, newPosY))
				        scalablePanel.revalidate()
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
		        val zoom = e.rotation * 0.1f
				scalablePanel.setZoom(scalablePanel.getZoom() - zoom)
		    }
		    
		}
		case e:MousePressed => {
		    if(e.source == this && e.peer.getButton() == MouseEvent.BUTTON1){
		        prevMouseDragPos = e.point
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
		        val xDrag = e.point.getX() - prevMouseDragPos.getX()
		        val yDrag = e.point.getY() - prevMouseDragPos.getY()
		        horizontalScrollBar.value -= xDrag.asInstanceOf[Int]
		        verticalScrollBar.value -= yDrag.asInstanceOf[Int]
		        prevMouseDragPos = e.point
		    }
		}
	}

}

