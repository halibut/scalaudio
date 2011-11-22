package org.instabetter.scalaudio
package components
package io

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.Mixer
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Line
import javax.sound.sampled.SourceDataLine
import javax.sound.sampled.DataLine


class AudioOut(implicit sp:SignalProperties) extends Component(sp) {
	val inputs = new SingleLineIOModule[Signal]()
    val outputs = new NoLineIOModule[Signal]()
    val controls = new NoLineIOModule[Control]()
    
    private var _outputDevice:Option[SourceDataLine] = None
    private var _skippedSamples = 0L
    
    private var _tempBuffer:Array[Byte] = null
    private var _bufferSamplePosition = 0
	
    def getSkippedSamples() = _skippedSamples
    
    def setOutputDevice(mixer:Mixer){
	    require(!inputs.lines.isEmpty, "You must define an output signal (line) before setting the output device.")
	        
	    val channels = inputs.line.numChannels
	    val audioFormat = AudioIO.createAudioFormat(sp, channels)
	    val lineInfo = AudioIO.createOutputLineInfo(sp, audioFormat)
	    _outputDevice = Option(mixer.getLine(lineInfo).asInstanceOf[SourceDataLine])
	    _outputDevice.foreach{line =>
	        line.open(audioFormat)
	        line.start()
	    }
	    
	    _tempBuffer = new Array(sp.bytesPerChannel * channels * (sp.maxDelaySamples / 100).asInstanceOf[Int])
	}
	
    def step(): Unit = {
        _outputDevice.foreach{line =>
            val signal = inputs.line.read
            
            val buffered = line.getBufferSize() - line.available()
            if(buffered < sp.maxDelaySamples){
	            val signalAsByteArray = AudioIO.getSignalAsByteArray(signal, sp.bytesPerChannel)
	            for(ind <- 0 until signalAsByteArray.size ){
	                _tempBuffer(_bufferSamplePosition + ind) = signalAsByteArray(ind)
	            }
	            _bufferSamplePosition += signalAsByteArray.size
            }
            else{
                _skippedSamples += 1
            }
            
            if(_bufferSamplePosition == _tempBuffer.size){
                line.write(_tempBuffer, 0, _tempBuffer.size)
                _bufferSamplePosition = 0
            }
        }
    }
}
