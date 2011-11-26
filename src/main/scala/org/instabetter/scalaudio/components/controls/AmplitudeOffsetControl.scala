package org.instabetter.scalaudio
package components
package controls

trait AmplitudeOffsetControl {
self:ComponentControls with ComponentOutputs =>
    
    val amplitudeOffsetControl = new Control()
    amplitudeOffsetControl.name = "Amplitude Offset"
    amplitudeOffsetControl.description = "Adds the specified offset to the signal."
    amplitudeOffsetControl.write(0.0f)
    
    //Add the control to the list of controls
    this.addControl(amplitudeOffsetControl)
    //Add the post processor to the output post processors
    this.addPostProcessor(signalProcessor)
    
    def setAmplitudeOffset(gain:Float){ amplitudeOffsetControl.write(gain) }
    def getAmplitudeOffset():Float = { amplitudeOffsetControl.read() }
    
    private def signalProcessor(signal:Seq[Float]):Seq[Float] = {
        signal.map{ _ + getAmplitudeOffset() }
    }
}