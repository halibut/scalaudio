package org.instabetter.scalaudio
package components

abstract class Component(val signalProperties:SignalProperties) extends Identity{
    
    protected def preProcess():Unit = {};
    protected def process():Unit;
    protected def postProcess():Unit = {};
    
    def processSignal(){
        preProcess()
        process()
        postProcess()
    }
}

trait ComponentControls {
self:Component =>

    private var _controls:Vector[Control] = Vector()
        
    def controls():IndexedSeq[Control] = _controls
    
    protected def addControl(control:Control){ _controls :+= control }
    protected def removeControl(control:Control){ _controls = _controls.filterNot(_ eq control) }
    protected def removeControl(index:Int){ removeControl(_controls(index)) }
    protected def removeControl(name:String){ _controls = _controls.filterNot(_.name eq name) }
}

