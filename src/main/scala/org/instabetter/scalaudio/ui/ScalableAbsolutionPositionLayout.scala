package org.instabetter.scalaudio.ui

import java.awt.LayoutManager2
import java.awt.{Component => AWTComponent}
import java.awt.{Container => AWTContainer}
import java.awt.Dimension
import javax.naming.OperationNotSupportedException

object ScalableAbsPositionLayout{
    val MAX_DIM = new Dimension(Int.MaxValue, Int.MaxValue)
    val MIN_DIM = new Dimension(0, 0)
}

class ScalableAbsPositionLayout extends LayoutManager2{
    import ScalableAbsPositionLayout._
    private var _comps:Map[AWTComponent,PositionConstraint] = Map()
    private var _zoom = 1.0f
    
    def setZoom(zoom:Float) = {
        _zoom = math.max(0.05f, zoom)
    }
    def getZoom():Float = { _zoom }
    
    override def addLayoutComponent(comp:AWTComponent, constraints:Object){
        val const = constraints.asInstanceOf[PositionConstraint]
        
        val newConst = if(const.x < 0 || const.y < 0){
            val (newX,xOffset) = if(const.x < 0) (0, -const.x) else (const.x, 0)
            val (newY,yOffset) = if(const.y < 0) (0, -const.y) else (const.y, 0)
            
            //Adjust all existing components by moving them right and down
            _comps = _comps.map{ case (existingComp, pos) =>
                (existingComp -> new PositionConstraint(pos.x + xOffset, pos.y + yOffset))
            }
            
            new PositionConstraint(newX, newY)
        }
        else{
            const
        }
        _comps += (comp -> newConst)
    }
    override def addLayoutComponent(name:String, comp:AWTComponent){}
    override def removeLayoutComponent(comp:AWTComponent){ _comps -= comp }
    
    def getConstraints(comp:AWTComponent):PositionConstraint = { _comps(comp) }
    
    def getComponents() = { _comps.keySet }
    
    override def preferredLayoutSize(parent:AWTContainer):Dimension = {
        var maxX = Int.MinValue
        var maxY = Int.MinValue
        for(compPos <- _comps){
            val (comp,pos) = compPos
            val rightPos = ((pos.x + comp.getPreferredSize().getWidth()) * _zoom).asInstanceOf[Int]
            val bottomPos = ((pos.y + comp.getPreferredSize().getHeight()) * _zoom).asInstanceOf[Int]
            maxX = math.max(maxX, rightPos)
            maxY = math.max(maxY, bottomPos)
        }
        new Dimension(maxX, maxY)
    }
    override def minimumLayoutSize(parent:AWTContainer):Dimension = { MIN_DIM }
    override def maximumLayoutSize(target:AWTContainer):Dimension = { MAX_DIM }

    override def getLayoutAlignmentX(target:AWTContainer):Float = { 0f }
    override def getLayoutAlignmentY(target:AWTContainer):Float = { 0f }

    override def invalidateLayout(target:AWTContainer){}
    
    override def layoutContainer(parent:AWTContainer){
        for(compPos <- _comps){
            val (comp,pos) = compPos
            
            val preferredSize = comp.getPreferredSize()
            val newWidth = (preferredSize.getWidth() * _zoom).asInstanceOf[Int]
            val newHeight = (preferredSize.getHeight() * _zoom).asInstanceOf[Int]
            
            val newX = (pos.x * _zoom).asInstanceOf[Int]
            val newY = (pos.y * _zoom).asInstanceOf[Int]
            
            comp.setBounds(newX, newY, newWidth, newHeight)
        }
    }
}

case class PositionConstraint(x:Int,y:Int)