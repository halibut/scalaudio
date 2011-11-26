package org.instabetter.scalaudio
package components
package controls

trait GainControl {
self:ComponentControls with ComponentOutputs =>
    
    val gainControl = new Control()
    gainControl.name = "Gain"
    gainControl.description = "Amplifies the output by the specified amount."
    gainControl.write(1.0f)
        
    //Add the control to the component's list of controls
    this.addControl(gainControl)
    //Add the processor to the output post-processors
    this.addPostProcessor(signalProcessor)
    
    def setGain(gain:Float){ gainControl.write(gain) }
    def getGain():Float = { gainControl.read() }
    
    private def signalProcessor(signal:Seq[Float]):Seq[Float] = {
        signal.map{ _ * getGain() }
    }
}