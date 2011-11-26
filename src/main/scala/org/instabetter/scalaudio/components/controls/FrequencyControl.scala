package org.instabetter.scalaudio
package components
package controls

trait FrequencyControl {
self:ComponentControls =>
    
    val frequencyControl = new Control()
    frequencyControl.name = "Frequency"
    frequencyControl.description = "Sets the frequency of the component."
    frequencyControl.write(0.0f)
    
    this.addControl(frequencyControl)
    
    def setFrequency(frequency:Float){ frequencyControl.write(frequency) }
    def getFrequency():Float = { frequencyControl.read() }
}