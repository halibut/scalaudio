package org.instabetter.scalaudio
package components
package siggen

class SineWaveGenerator(cycleOffset:Float = 0f)(implicit sp:SignalProperties) 
		extends SignalGenerator(sp,cycleOffset) {

    private val TWO_PI = math.Pi.asInstanceOf[Float] * 2f
    
    signalOutput.name = "Sine Wave Output"
    signalOutput.description = "The output signal from the sine wave generator."
    
    def signalFunc(cycle:Float):Float = {
        val sinInput = TWO_PI * cycle
        
        val signal = math.sin(sinInput).asInstanceOf[Float]
            
        signal
    }

}