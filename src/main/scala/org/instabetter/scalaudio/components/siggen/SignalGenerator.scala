package org.instabetter.scalaudio
package components
package siggen

abstract class SignalGenerator(sp:SignalProperties, timeOffset:Float) extends Component(sp) {
    val inputs = new NoLineIOModule[Signal]()
    val outputs = new SingleLineIOModule[Signal]()
    val controls = new SingleLineIOModule[Control]()

    private var _steps = math.round(timeOffset * sp.sampleRate).asInstanceOf[Long] -1L
    
    val frequencyControl = new Control()
    frequencyControl.name = "Frequency"
    frequencyControl.description = "Controls the frequency of the output signal."
        
    controls.setLine(frequencyControl)
    
    override def step():Unit = {
        _steps+=1L
        
        val signal = signalFunc(getFrequency(), _steps)
        
        outputs.lines.foreach(_.write(signal))
    }
    
    protected def signalFunc(frequency:Float, currentStep:Long):Float
    
    def setFrequency(frequency:Float){ frequencyControl.write(frequency) }
    def getFrequency():Float = { frequencyControl.read }
}
