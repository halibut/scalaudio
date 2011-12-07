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

import scala.collection.Seq
import javax.sound.sampled.SourceDataLine
import javax.sound.sampled.Mixer

class OutputDevice(val blockOnOutputDevice:Boolean = false) extends Component with ComponentInputs with AudioDevice[SourceDataLine] {
	private val INTERNAL_BUFFER_SIZE = 8
	private var _internalBuffer:Array[Byte] = null
	private var _internalBufferPos = 0
	private var _skippedSamples = 0L
	
	//Create the audio signal that contains the data to send to the output device
    val audioSignal = new Signal(2)
    addInput(audioSignal)
    
    //Add a pre-processor to cutoff the data beyond the range [-1.0 , 1.0]
    addPreProcessor{floatVal =>
	    math.max(-1.0f,math.min(floatVal,1.0f))
	}
    
    override def audioFormatChanged(){
        val currentChannels = audioSignal.channels()
        val audioFormat = _jsAudioFormat.get
        val newChannels = audioFormat.getChannels()
        
        if(currentChannels != newChannels){
            audioSignal.setNumChannels(newChannels)
        }
        
        _internalBuffer = new Array(audioFormat.getFrameSize() * INTERNAL_BUFFER_SIZE)
        _internalBufferPos = 0
    }
	
    override def getAvailableDrivers():Seq[Mixer] = {
        AudioDevice.getOutputDrivers()
    }

    override protected def internalGetDeviceLines():Seq[SourceDataLine] = {
        AudioDevice.getOutputDevices(_jsMixer.get).getOrElse(Seq())   
    }

    override protected def internalOpenLine(): Unit = {
        val line = _jsDataLine.get
        val audioFormat = _jsAudioFormat.get
        
        line.open(audioFormat, _preferredDeviceBufferSize * audioFormat.getFrameSize())
    }

    def getSkippedSamples() = _skippedSamples
    
    override protected def process(): Unit = {
        if(_jsDataLine.isEmpty || !_jsDataLine.get.isOpen())
            throw new IllegalStateException("An output device must be selected and opened before it can be written to.")
        
        //Get the Float array to write to the output device
        val signal = audioSignal.read()
        
        //Get a reference to the SourceDataLine (the device to output to)
        val line = _jsDataLine.get
        val audioFormat = _jsAudioFormat.get
        val frameSize = audioFormat.getFrameSize()
        val internalBufferSize = _internalBuffer.size
        
        //Convert the Float audio signal and write it to the temporary buffer
        AudioIO.copySignalSampleToByteArray(signal, 
                _internalBuffer, _internalBufferPos, 
                frameSize, audioFormat.isBigEndian())
        _internalBufferPos += frameSize
        
        //If the internal buffer is full, then write to the device
        if(_internalBufferPos == internalBufferSize){
            if(blockOnOutputDevice){
                //If we are okay with the output device blocking until more space is available
                //in its buffer, then just write the whole temp buffer to it
                line.write(_internalBuffer, 0, internalBufferSize)
            }
            else{
	            //Calculate the maximum amount of samples that we can write before the write blocks
                //and only write those samples. This may cause some samples to get "skipped".
                val bytesToWrite = math.min(internalBufferSize, line.available()) 
	            line.write(_internalBuffer, 0, bytesToWrite)
	            
	            _skippedSamples += (internalBufferSize - bytesToWrite) / frameSize
            }

            //Reset the internal buffer to start writing to it again
            _internalBufferPos = 0;
        }
    }
}