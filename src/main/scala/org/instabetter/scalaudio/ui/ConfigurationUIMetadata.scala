package org.instabetter.scalaudio
package ui

import components._

class ConfigurationUIMetadata {

    private var _compPositions:Map[Component,(Int,Int)] = Map()
    
    
    def getComponentPosition(comp:Component):Option[(Int,Int)] = {
        _compPositions.get(comp)
    }
    
    def setComponentPosition(comp:Component,position:(Int,Int)){
        _compPositions += (comp -> position)
    }
}