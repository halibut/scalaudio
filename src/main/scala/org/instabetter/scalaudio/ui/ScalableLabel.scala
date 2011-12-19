package org.instabetter.scalaudio.ui;

import scala.swing.Label
import scala.swing.event.UIElementResized
import java.awt.Dimension

class ScalableLabel(labelText:String, fontSize:Float) 
	extends Label(labelText) {
    private var _fontSize = fontSize
    private var _origSize:Dimension = null

    this.font = font.deriveFont(_fontSize)
    
    listenTo(this)
    
    this.reactions += {
        case e:UIElementResized =>{
            if(_origSize == null){
                _origSize = this.size
            }
            else{
                val newSize = this.size
                val ratioX = newSize.width / _origSize.width.asInstanceOf[Float]
                val ratioY = newSize.height / _origSize.height.asInstanceOf[Float]
                
                val ratio = math.min(ratioX, ratioY)
                val newFontSize = _fontSize * ratio
                this.font = this.font.deriveFont(newFontSize)
            }
        }
    }
	
}
