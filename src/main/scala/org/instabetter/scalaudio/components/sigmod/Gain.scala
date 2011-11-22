package org.instabetter.scalaudio
package components
package sigmod

class Gain(implicit sp:SignalProperties) extends Component(sp){
	val inputs = new SingleLineIOModule[Signal]()
	val outputs = new SingleLineIOModule[Signal]()
	val controls = new SingleLineIOModule[Control]()
    
    name = "Gain"
    description = "Multiplies signal(s) amplitude(s) by the specified value."
        
    val gainControl = new Control()
	gainControl.name = "Gain Control"
	gainControl.description = "Controls the signal gain."
	controls.setLine(gainControl)    
	    
	def setGain(gain:Float){ gainControl.write(gain) }
	def getGain():Float = { gainControl.read() }
	
	def setInputSignal(inputSignal:Signal){ inputs.setLine(inputSignal) }
	def setOutputSignal(outputSignal:Signal){ outputs.setLine(outputSignal) }
	
	override def step():Unit = {
	    val gain = getGain()
	    inputs.lines.zip(outputs.lines).foreach{
	        case (inputSignal, outputSignal) =>
	            outputSignal.write(inputSignal.read().map{signal =>
	                signal * gain
	            })
	    }
	}
}