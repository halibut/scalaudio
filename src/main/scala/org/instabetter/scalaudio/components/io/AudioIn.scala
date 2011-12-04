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

import javax.sound.sampled.TargetDataLine
import javax.sound.sampled.Mixer
import scala.collection.mutable.Queue
import java.awt.image.SampleModel

/**
 * This component will read in an audio signal from a physical output device (microphone, 
 * line in, etc...) using the JavaSound sampled API.
 * 
 * @param numChannels The number of channels in the audio signal
 * @param inputBufferSize The number of samples to wait for before reading from the input device. (must be >= 1)
 * @param deviceBufferSize Controls the internal buffer size of the input device (must be >= outputBufferSize) 
 * @param blockOnInputDevice If true, the process() method will wait on the input device until
 * there is enough data available in its internal buffer to read inputDataSize samples. If false, and there are 
 * not enough samples available in the internal buffer, the component will simply skip until inputDataSize samples
 * become available, preventing the process() method from blocking. The result is that the input audio may sound 
 * garbled, but it won't stop the calling thread in performance-critical scenarios.
 */
class AudioIn(numChannels:Int = 1,
        inputBufferSize:Int = 8, 
        deviceBufferSize:Int = 1600,
        deviceBigEndianFormat:Boolean = true,
        val blockOnInputDevice:Boolean = false)
        (implicit sp:SignalProperties) extends Component(sp) with ComponentOutputs {
	private var _inputDevice:Option[TargetDataLine] = None
    private var _skippedSamples = 0L
    
    private val _sampleSizeInBytes = sp.bytesPerChannel * numChannels
    private var _maxBufferBytes = deviceBufferSize * _sampleSizeInBytes
    private val _tempBuffer:Array[Byte] = new Array(inputBufferSize * _sampleSizeInBytes)
	private var _bufferSamplePosition = _tempBuffer.size
	private val _sampleArray:Array[Float] = new Array(numChannels)
	
    def getSkippedSamples() = _skippedSamples
    
    val audioSignal = new OutputSignal(numChannels)
	addOutput(audioSignal)
    
    def setInputDevice(mixer:Mixer){
	    val audioFormat = AudioIO.createAudioFormat(sp, numChannels, deviceBigEndianFormat)
	    val lineInfo = AudioIO.createInputLineInfo(audioFormat, deviceBufferSize)
	    _inputDevice = Option(mixer.getLine(lineInfo).asInstanceOf[TargetDataLine])
	    println(_inputDevice)
	    _inputDevice.foreach{line =>
	        line.open(audioFormat)
	        line.start()
	    }
	    
	    if(_inputDevice.isEmpty){
	        throw new IllegalStateException("An input device could not be found with the specified settings: "+lineInfo)
	    }
	}
    
    override protected def process(): Unit = {
        if(_inputDevice.isEmpty)
            throw new IllegalStateException("An output device must be set before it can be written to.")
        
        //Get a reference to the TargetDataLine (the device we're getting input from)
        val line = _inputDevice.get 

        if(_bufferSamplePosition == _tempBuffer.size){
            val avail = line.available()
            
            //We need to read from the input device
            if(avail >= _tempBuffer.size || blockOnInputDevice){
                //If it is okay to wait for the input device to fill up, or if the requested amount
                //of data is available to read, then call the read method (this will block if the 
                //requested amount of data is not available yet)
                line.read(_tempBuffer, 0, _tempBuffer.size)
                _bufferSamplePosition = 0
            }
            else{
                //Otherwise, do nothing. If we don't want to block, then there's not enough
                //data available for reading.
            }
        }
        
        
        if(_bufferSamplePosition < _tempBuffer.size){
            //If we have data in our buffer, read and convert the next sample from our 
            //temp buffer and write it to the _sampleArray
	        AudioIO.copyByteArrayToSignalSample(_tempBuffer, _bufferSamplePosition, _sampleArray, sp.bytesPerChannel, deviceBigEndianFormat)
	        _bufferSamplePosition += _sampleSizeInBytes
        }
        else{
            //Otherwise, there is no data available to read, so just write 0.0f to the 
            //_sampleArray
            var i = 0
            while(i < numChannels){
            	_sampleArray(i) = 0.0f
            }
            _skippedSamples += 1
        }

        //Write the value of _sampleArray to the output audioSignal
        audioSignal.write(_sampleArray)       
    }
}