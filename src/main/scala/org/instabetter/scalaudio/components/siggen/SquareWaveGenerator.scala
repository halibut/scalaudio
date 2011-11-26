package org.instabetter.scalaudio
package components
package siggen

class SquareWaveGenerator(cycleOffset:Float = 0f)(implicit sp:SignalProperties) 
		extends SignalGenerator(sp,cycleOffset) {

    signalOutput.name = "Square Wave Output"
    signalOutput.description = "The output signal from the square wave generator."
    
    def signalFunc(cycle:Float):Float = {
        if(cycle < 0.5f)
            -1f
        else
            1f
    }

}