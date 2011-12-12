package org.instabetter.scalaudio.ui

import javax.swing.JPanel
import java.awt.Graphics2D
import java.awt.Graphics
import scala.swing.Panel

class ConfigurationCanvas extends Panel{
    private var _zoom = 1.0f
    private var _offset = (0.0f,0.0f)

    
    override def paintComponent(g:Graphics2D){
        val size = this.size
        val halfWidth = size.width / 2.0f
        val halfHeight = size.height / 2.0f
        
        g.translate(halfWidth + _offset._1, halfHeight + _offset._2)
        g.scale(_zoom,_zoom)
        g.translate(-halfWidth, -halfHeight)
    }
}