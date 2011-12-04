/*
 * Copyright (C) 2011 instaBetter Software <http://insta-better.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.instabetter.scalaudio
package components
package io

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.Mixer
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Line
import javax.sound.sampled.SourceDataLine
import javax.sound.sampled.DataLine


/**
 * This component will send an audio signal to physical output device (speakers, etc...)
 * using the JavaSound sampled API.
 * 
 * @param numChannels The number of channels in the audio signal
 * @param outputBufferSize The number of samples to buffer before writing to the output device. (must be >= 1)
 * @param deviceBufferSize Controls the internal buffer size of the output device (must be >= outputBufferSize) 
 * @param blockOnOutputDevice If true, the process() method will wait on the output device until
 * there is room in its internal buffer to write all the data. If false, any samples that cannot fit into
 * the output device's internal buffer will be dropped, preventing the process() method from blocking. The 
 * result is that the audio may sound garbled, but it won't stop the calling thread in performance-critical
 * scenarios.
 */
class AudioOut(val numChannels:Int = 2, 
        outputBufferSize:Int = 32, 
        deviceBufferSize:Int = 6000,
        deviceBigEndianFormat:Boolean = true,
        val blockOnOutputDevice:Boolean = false)
		(implicit sp:SignalProperties) extends Component(sp) with ComponentInputs {
    
    require(outputBufferSize >= 1, "outputBufferSize must be >= 1. Found " + outputBufferSize)
    require(outputBufferSize <= deviceBufferSize, "outputBufferSize must be <= deviceBufferSize. Found output:"+outputBufferSize+" device:"+deviceBufferSize)
    
	private var _outputDevice:Option[SourceDataLine] = None
    private var _skippedSamples = 0L
    
    private var _bufferSamplePosition = 0
    private val _sampleSizeInBytes = sp.bytesPerChannel * numChannels
    private var _maxBufferBytes = deviceBufferSize * _sampleSizeInBytes
    private val _tempBuffer:Array[Byte] = new Array(outputBufferSize * _sampleSizeInBytes)
    
    def getSkippedSamples() = _skippedSamples
    
    val audioSignal = new Signal(numChannels)
	addInput(audioSignal)
    
    def setOutputDevice(mixer:Mixer){
	    val audioFormat = AudioIO.createAudioFormat(sp, numChannels, deviceBigEndianFormat)
	    val lineInfo = AudioIO.createOutputLineInfo(audioFormat, deviceBufferSize)
	    _outputDevice = Option(mixer.getLine(lineInfo).asInstanceOf[SourceDataLine])
	    _outputDevice.foreach{line =>
	        line.open(audioFormat, _maxBufferBytes)
	        line.start()
	        
	        _maxBufferBytes = line.getBufferSize()
	    }
	    
	    if(_outputDevice.isEmpty){
	        throw new IllegalStateException("An output device could not be found with the specified settings: "+lineInfo)
	    }
	}
	
    override protected def process(): Unit = {
        if(_outputDevice.isEmpty)
            throw new IllegalStateException("An output device must be set before it can be written to.")
        
        //Get the signal value to write
        val signal = audioSignal.read
        
        //Get a reference to the SourceDataLine (the device to output to)
        val line = _outputDevice.get 
        
        //Convert the Float audio signal and write it to the temporary buffer
        AudioIO.copySignalSampleToByteArray(signal, _tempBuffer, _bufferSamplePosition, sp.bytesPerChannel, deviceBigEndianFormat)
        _bufferSamplePosition += _sampleSizeInBytes
        
        //If the temp buffer is full, then write to the device
        if(_bufferSamplePosition == _tempBuffer.size){
            if(blockOnOutputDevice){
                //If we are okay with the output device blocking until more space is available
                //in its buffer, then just write the whole temp buffer to it
                line.write(_tempBuffer, 0, _tempBuffer.size)
            }
            else{
	            //Calculate the maximum amount of samples that we can write before the write blocks
                //and only write those samples. This may cause some samples to get "skipped".
	            val bufferedInLine = _maxBufferBytes - line.available()
	            val bytesToWrite = math.min(_tempBuffer.size, _maxBufferBytes - bufferedInLine) 
	            
	            
	            line.write(_tempBuffer, 0, bytesToWrite)
	            
	            _skippedSamples += (_tempBuffer.size - bytesToWrite) / _sampleSizeInBytes
            }

            //Reset the temp buffer to start writing to it again
            _bufferSamplePosition = 0;
        }
    }
}

