package org.instabetter.scalaudio
package components
package siggen

class SineWaveGenerator(timeOffset:Float = 0f)(implicit sp:SignalProperties) 
		extends SignalGenerator(sp,timeOffset) {

    private val TWO_PI = math.Pi.asInstanceOf[Float] * 2f
    
    def signalFunc(frequency:Float, currentStep:Long):Float = {
        val sinFreqAdj = frequency / sp.sampleRate
        val cycles = currentStep * sinFreqAdj 
        val sinInput = TWO_PI * cycles
        
        val signal = math.sin(sinInput).asInstanceOf[Float]
            
        signal
    }

}