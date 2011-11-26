package org.instabetter.scalaudio
package components

trait ComponentInputs {
self:Component =>
	
    private var _signalPreProcessors:Vector[Function1[Seq[Float],Seq[Float]]] = Vector()
    private var _inputs:Vector[Signal] = Vector()
        
    def inputs():IndexedSeq[Signal] = _inputs
    
    protected def addInput(input:Signal){ _inputs :+= input }
    protected def removeInput(input:Signal){ _inputs = _inputs.filterNot(_ eq input) }
    protected def removeInput(index:Int){ removeInput(_inputs(index)) }
    protected def removeInput(name:String){ _inputs = _inputs.filterNot(_.name eq name) }
    
    def addPreProcessor(func:(Seq[Float])=>Seq[Float]) = { 
        _signalPreProcessors :+= func 
    }
    def removePreProcessor(func:(Seq[Float])=>Seq[Float]) = {
        _signalPreProcessors = _signalPreProcessors.filterNot(_ eq func)
    }
    
    override def preProcess(){
        _inputs.foreach{input=>
            _signalPreProcessors.foreach{processor=>
            	input.write(processor(input.read()))
            }
        }
    }
}
