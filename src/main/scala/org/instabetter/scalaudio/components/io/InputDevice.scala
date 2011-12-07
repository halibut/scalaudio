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
import javax.sound.sampled.Mixer
import javax.sound.sampled.TargetDataLine

class InputDevice(val blockOnInputDevice:Boolean = false) extends Component with ComponentOutputs with AudioDevice[TargetDataLine] {
	private val INTERNAL_BUFFER_SIZE = 8
	private var _internalBuffer:Array[Byte] = null
	private var _internalBufferPos = 0
	private var _skippedSamples = 0L
	private var _sampleArray:Array[Float] = null
	
	//Create the audio signal that contains the data to send to the output device
    val audioSignal = new OutputSignal(1)
    addOutput(audioSignal)
    
    override def audioFormatChanged(){
        val currentChannels = audioSignal.channels()
        val audioFormat = _jsAudioFormat.get
        val newChannels = audioFormat.getChannels()
        
        if(currentChannels != newChannels){
            audioSignal.setNumChannels(newChannels)
        }
        
        _internalBuffer = new Array(audioFormat.getFrameSize() * INTERNAL_BUFFER_SIZE)
        _internalBufferPos = _internalBuffer.size
        _sampleArray = new Array(audioFormat.getChannels())
    }
	
    override def getAvailableDrivers():Seq[Mixer] = {
        AudioDevice.getInputDrivers()
    }

    override protected def internalGetDeviceLines():Seq[TargetDataLine] = {
        AudioDevice.getInputDevices(_jsMixer.get).getOrElse(Seq())   
    }

    override protected def internalOpenLine(): Unit = {
        val line = _jsDataLine.get
        val audioFormat = _jsAudioFormat.get
        
        line.open(audioFormat, _preferredDeviceBufferSize * audioFormat.getFrameSize())
    }

    def getSkippedSamples() = _skippedSamples
    
    override protected def process(): Unit = {
        if(_jsDataLine.isEmpty || !_jsDataLine.get.isOpen())
            throw new IllegalStateException("An input device must be selected and opened before it can be readFrom.")
        
        //Get a reference to the SourceDataLine (the device to output to)
        val line = _jsDataLine.get
        val audioFormat = _jsAudioFormat.get
        val frameSize = audioFormat.getFrameSize()
        val internalBufferSize = _internalBuffer.size
        
        //Check if the internal buffer is empty, and if we need
        //to read some data from the device.
        if(_internalBufferPos == internalBufferSize){
            val avail = line.available()
            
            //We need to read from the input device
            if(avail >= _internalBufferPos || blockOnInputDevice){
                //If it is okay to wait for the input device to fill up, or if the requested amount
                //of data is available to read, then call the read method (this will block if the 
                //requested amount of data is not available yet)
                line.read(_internalBuffer, 0, internalBufferSize)
                _internalBufferPos = 0
            }
            else{
                //Otherwise, do nothing. If we don't want to block, then there's not enough
                //data available for reading.
            }
        }
        
        //If we have data in the internal buffer, then convert to Floats and 
        //write to our sample array.
        if(_internalBufferPos < internalBufferSize){
            //If we have data in our buffer, read and convert the next sample from our 
            //temp buffer and write it to the _sampleArray
	        AudioIO.copyByteArrayToSignalSample(
	                _internalBuffer, _internalBufferPos, 
	                _sampleArray, 
	                frameSize, audioFormat.isBigEndian())
	        _internalBufferPos += frameSize
        }
        else{
            //Otherwise, there is no data available to read, so just write 0.0f to the 
            //_sampleArray
            var i = 0
            while(i < audioFormat.getChannels()){
            	_sampleArray(i) = 0.0f
            	i+=1
            }
            _skippedSamples += 1
        }

        //Write the value of _sampleArray to the output audioSignal
        audioSignal.write(_sampleArray) 
    }
}