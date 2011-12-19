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

/**
 * A Component that sends an audio signal to an output audio device like speakers or a line-out.
 * This component optionally blocks when sending audio to the output device. This can occur
 * when more data is written to the output device buffer than can be sent to the physical 
 * hardware (speakers, etc...). Blocking can be useful if the audio signal is not synchronized
 * with the output device sample rate, such as when reading audio data from a file.
 * 
 * There will always be some output latency. The actual latency is governed by the 
 * audio driver (and underlying audio output hardware). 
 *
 * @param blockOnOutputDevice Tells the component to block if the output device's buffer
 * becomes full. If false, then it's possible that the output device might skip samples
 * in the audio sample
 */
class OutputDevice(val blockOnOutputDevice:Boolean = false) 
	extends Component with ComponentInputs with ComponentControls with AudioDevice[SourceDataLine] {
	private val INTERNAL_BUFFER_SIZE = 8
	private var _internalBuffer:Array[Byte] = null
	private var _internalBufferPos = 0
	private var _skippedSamples = 0L
	
	//Create the audio signal that contains the data to send to the output device
    val audioSignal = new InputSignal(this, 2)
    addInput(audioSignal)
    
    //Add a pre-processor to cutoff the data beyond the range [-1.0 , 1.0]
    addPreProcessor{floatVal =>
	    math.max(-1.0f,math.min(floatVal,1.0f))
	}
    
    override def audioFormatChanged(){
        val currentChannels = audioSignal.channels()
        val audioFormat = getAudioFormat().get
        val newChannels = audioFormat.getChannels()
        
        if(currentChannels != newChannels){
            audioSignal.setNumChannels(newChannels)
        }
        
        _internalBuffer = new Array(audioFormat.getFrameSize() * INTERNAL_BUFFER_SIZE)
        _internalBufferPos = 0
    }
	
    override protected def getAvailableDrivers():IndexedSeq[Mixer] = {
        AudioDevice.getOutputDrivers()
    }

    override protected def internalGetDeviceLines():IndexedSeq[SourceDataLine] = {
        AudioDevice.getOutputDevices(getDriver().get).getOrElse(IndexedSeq())   
    }

    override protected def internalOpenLine(): Unit = {
        val line = getLine().get
        val audioFormat = getAudioFormat().get
        
        line.open(audioFormat, _preferredDeviceBufferSize * audioFormat.getFrameSize())
    }

    def getSkippedSamples() = _skippedSamples
    
    override def unpause(){
        getLine().foreach{line =>
            line.start()
        }
    }
    
    override def pause(){
        getLine().foreach{line =>
            line.stop()
            line.flush()
        }
    }
    
    override protected def process(): Unit = {
        val lineOpt = getLine()
        if(lineOpt.isEmpty || !lineOpt.get.isOpen())
            throw new IllegalStateException("An output device must be selected and opened before it can be written to.")
        
        //Get the Float array to write to the output device
        val signal = audioSignal.read()
        
        //Get a reference to the SourceDataLine (the device to output to)
        val line = lineOpt.get
        val audioFormat = getAudioFormat().get
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