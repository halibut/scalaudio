package org.instabetter.scalaudio
package components
package siggen
import org.instabetter.scalaudio.components.controls.FrequencyControl
import org.instabetter.scalaudio.components.controls.AmplitudeOffsetControl
import org.instabetter.scalaudio.components.controls.GainControl

abstract class SignalGenerator(sp:SignalProperties, cycleOffset:Float) 
	extends Component(sp) with ComponentOutputs with ComponentControls
	with FrequencyControl {
    
    private var _cycle = cycleOffset % 1.0f
    
    val signalOutput = new OutputSignal(1)
    addOutput(signalOutput)
    
    override protected def process():Unit = {
        val signal = signalFunc(_cycle)
        
        _cycle += getFrequency() * sp.inverseSampleRate
        
        //Make sure cycle stays between 0 and 1.0
        if(_cycle > 1.0f)
            _cycle = _cycle % 1.0f
        
        outputs().foreach(_.write(signal))
    }
    
    protected def signalFunc(cyclePos:Float):Float
    
}
