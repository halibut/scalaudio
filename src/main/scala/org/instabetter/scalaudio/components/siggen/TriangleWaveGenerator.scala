package org.instabetter.scalaudio
package components
package siggen

class TriangleWaveGenerator(cycleOffset:Float = 0f)(implicit sp:SignalProperties) 
		extends SignalGenerator(sp,cycleOffset) {

    signalOutput.name = "Triangle Wave Output"
    signalOutput.description = "The output signal from the triangle wave generator."
    
    def signalFunc(cycle:Float):Float = {
        if(cycle < 0.5f)
        	-1f + (4.0f * cycle)
        else
            1f - (4.0f * (cycle-.5f))
    }

}