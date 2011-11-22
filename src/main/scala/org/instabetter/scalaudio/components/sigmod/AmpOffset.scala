package org.instabetter.scalaudio
package components
package sigmod


class AmpOffset(implicit sp:SignalProperties) extends Component(sp){
	val inputs = new SingleLineIOModule[Signal]()
	val outputs = new SingleLineIOModule[Signal]()
	val controls = new SingleLineIOModule[Control]()
    
    name = "Amplitude Offset"
    description = "Adds a value to the signal."
        
    val offsetControl = new Control()
	offsetControl.name = "Offset Control"
	offsetControl.description = "Controls the signal amplitude offset."
	controls.setLine(offsetControl)    
	    
	def setOffset(gain:Float){ offsetControl.write(gain) }
	def getOffset():Float = { offsetControl.read() }
	
	def setInputSignal(inputSignal:Signal){ inputs.setLine(inputSignal) }
	def setOutputSignal(outputSignal:Signal){ outputs.setLine(outputSignal) }
	
	override def step():Unit = {
	    val offset = getOffset()
	    inputs.lines.zip(outputs.lines).foreach{
	        case (inputSignal, outputSignal) =>
	            outputSignal.write(inputSignal.read().map{signal =>
	                signal + offset
	            })
	    }
	}
}