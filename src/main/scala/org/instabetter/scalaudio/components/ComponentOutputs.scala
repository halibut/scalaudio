package org.instabetter.scalaudio
package components

trait ComponentOutputs {
self:Component =>

    private var _signalPostProcessors:Vector[Function1[Seq[Float],Seq[Float]]] = Vector()
    private var _outputs:Vector[OutputSignal] = Vector()
        
    def outputs():IndexedSeq[OutputSignal] = _outputs
    
    protected def addOutput(output:OutputSignal){ _outputs :+= output }
    protected def removeOutput(output:Signal){ _outputs = _outputs.filterNot(_ eq output) }
    protected def removeOutput(index:Int){ removeOutput(_outputs(index)) }
    protected def removeOutput(name:String){ _outputs = _outputs.filterNot(_.name eq name) }
    
    def addPostProcessor(func:(Seq[Float])=>Seq[Float]) = { 
        _signalPostProcessors :+= func 
    }
    def removePostProcessor(func:(Seq[Float])=>Seq[Float]) = {
        _signalPostProcessors = _signalPostProcessors.filterNot(_ eq func)
    }
    
    override def postProcess(){
        _outputs.foreach{output=>
            _signalPostProcessors.foreach{processor=>
            	output.write(processor(output.read()))
            }
            
            output.sendSignalToWires()
        }
    }
}