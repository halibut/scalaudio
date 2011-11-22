package org.instabetter.scalaudio
package components
package siggen

class SquareWaveGenerator(timeOffset:Float = 0f)(implicit sp:SignalProperties) 
		extends SignalGenerator(sp,timeOffset) {

    def signalFunc(frequency:Float, currentStep:Long):Float = {
        val remainder = currentStep.asInstanceOf[Float] % frequency
        
        if(remainder < frequency / 2f)
            -1f
        else
            1f
    }

}