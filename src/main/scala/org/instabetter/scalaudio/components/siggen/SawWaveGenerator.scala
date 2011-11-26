package org.instabetter.scalaudio
package components
package siggen

class SawWaveGenerator(cycleOffset:Float = 0f)(implicit sp:SignalProperties) 
		extends SignalGenerator(sp,cycleOffset) {

    signalOutput.name = "Saw Wave Output"
    signalOutput.description = "The output signal from the saw wave generator."
    
    def signalFunc(cycle:Float):Float = {
        -1f + (2.0f * cycle)
    }

}