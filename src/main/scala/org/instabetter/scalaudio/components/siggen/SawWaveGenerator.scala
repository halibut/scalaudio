package org.instabetter.scalaudio
package components
package siggen

class SawWaveGenerator(timeOffset:Float = 0f)(implicit sp:SignalProperties) 
		extends SignalGenerator(sp,timeOffset) {

    def signalFunc(frequency:Float, currentStep:Long):Float = {
        val remainder = currentStep.asInstanceOf[Float] % frequency
        
        -1f + (2.0f * remainder / frequency)
    }

}