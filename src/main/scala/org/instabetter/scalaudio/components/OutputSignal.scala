package org.instabetter.scalaudio
package components
import org.instabetter.scalaudio.components.convert.SignalDrivenControl

class OutputSignal(channels:IndexedSeq[Channel]) extends Signal(channels){
    def this(numChannels:Int){
        this((0 until numChannels).map{(ind) => new Channel()})
    }
    
    private var _wires:Vector[Signal] = Vector()
    
    def sendSignalToWires(){
        val signalVal = read()
        _wires.foreach{ _.write(signalVal) }
    }
    
    def wireTo(signal:Signal){ _wires :+= signal }
    def wireTo(control:Control){ wireTo(new SignalDrivenControl(control)) }
    def -->(signal:Signal){ wireTo(signal) }
    def -->(control:Control){ wireTo(control) }
    
}