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

/**
 * A Component that reads input from an audio device like a microphone or a line-in.
 * This component blocks while waiting for input. Since the input is buffered, 
 * there will always be some input latency. The actual latency is governed by the 
 * audio driver (and underlying audio capture hardware). The latency can be
 * affected somewhat by the parameters maxBufferedSamples and initBufferedSamples.
 * 
 * @param samplesPerRead controls how many samples will be read from the input device at 
 * a time. Lower numbers reduce the latency, larger numbers reduce the amount of native
 * calls to the underlying OS. This also affects the blocking nature of the InputDevice.
 * A value of 1 for samplesPerRead could potentially cause the processSignal() method to
 * block for every sample, while a value of 8 would only block every 8 samples. 
 * @param initBufferedSampes Sets the initial number of samples that will be buffered
 * before the InputDevice starts sending data to the output signal. Higher numbers add
 * more latency, but can increase throughput of the entire AudioConfiguration that the
 * InputDevice is a part of because it helps alleviate the unstable CPU scheduling
 * that occurs in a non-realtime operating system.
 * @param maxBufferedSamples The maximum number of samples to buffer before the component
 * starts dropping parts of the audio signal. A lower number reduces the maximum (software) 
 * latency that the component adds to the overall (hardware + software) latency of the 
 * input device. But beware that setting it too low might result in "skipping" of the
 * input audio.
 */
class InputDevice(val samplesPerRead:Int = 8, val initBufferedSamples:Int = 64, val maxBufferedSamples:Int=3000) 
		extends Component with ComponentOutputs with ComponentControls with AudioDevice[TargetDataLine] {
    
    require((1 <= samplesPerRead) && 
            (samplesPerRead <= initBufferedSamples) &&
            (initBufferedSamples <= maxBufferedSamples), 
            "Relationship between samplesPerRead, initBufferedSamples, and maxBufferedSamples must be: "+
            "1 <= samplesPerRead <= initBufferedSamples <= maxBufferedSamples; But found: samplesPerRead: "+
            samplesPerRead+", initBufferedSampes:"+initBufferedSamples+", maxBufferedSamples:"+maxBufferedSamples)
    
	private var _internalBuffer:Array[Byte] = null
	private var _internalBufferPos = 0
	private var _skippedSamples = 0L
	private var _sampleArray:Array[Float] = null
	
	//Create the audio signal that stores the data read from the input device
    val audioSignal = new OutputSignal(this, 1)
    addOutput(audioSignal)
    
    override def audioFormatChanged(){
        val currentChannels = audioSignal.channels()
        val audioFormat = getAudioFormat().get
        val newChannels = audioFormat.getChannels()
        
        if(currentChannels != newChannels){
            audioSignal.setNumChannels(newChannels)
        }
        
        _internalBuffer = new Array(audioFormat.getFrameSize() * samplesPerRead)
        _internalBufferPos = _internalBuffer.size
        _sampleArray = new Array(audioFormat.getChannels())
    }
	
    override protected def getAvailableDrivers():IndexedSeq[Mixer] = {
        AudioDevice.getInputDrivers()
    }

    override protected def internalGetDeviceLines():IndexedSeq[TargetDataLine] = {
        AudioDevice.getInputDevices(getDriver().get).getOrElse(IndexedSeq())   
    }

    override protected def internalOpenLine(): Unit = {
        val line = getLine().get
        val audioFormat = getAudioFormat().get
        
        line.open(audioFormat, _preferredDeviceBufferSize * audioFormat.getFrameSize())
    }

    def getSkippedSamples() = _skippedSamples
    
    override def unpause(){
        for(line <- getLine(); audioFormat <- getAudioFormat()){
            line.start()
            while(line.available() < initBufferedSamples * getAudioFormat().get.getFrameSize()){
                //Infinite loop until there's enough samples buffered in
                //the input device
                //Note: yield is surrounded by ` (back single-quote) because yield is a 
                //reserved keyword in Scala
                Thread.`yield`()
            }
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
            throw new IllegalStateException("An input device must be selected and opened before it can be readFrom.")
        
        //Get a reference to the SourceDataLine (the device to output to)
        val line = lineOpt.get
        val audioFormat = getAudioFormat().get
        val frameSize = audioFormat.getFrameSize()
        val internalBufferSize = _internalBuffer.size
        
        //Check if the internal buffer is empty, and if we need
        //to read some data from the device.
        if(_internalBufferPos == internalBufferSize){
            val avail = line.available()
            
            //If the amount of data in the device's input
            //buffer is greater than the maximum buffered samples,
            //Then read in a bunch of data and throw it away
            //This will cause skipping!
            if(avail > maxBufferedSamples * frameSize){
                var newAvail = avail
                val targetBufferedSamples = (maxBufferedSamples - initBufferedSamples) / 2
                while(newAvail > targetBufferedSamples * frameSize){
                    line.read(_internalBuffer, 0, internalBufferSize)
                    newAvail -= internalBufferSize
                    _skippedSamples += internalBufferSize
                }
            }
            
            //Read some data from the input device. This call is blocking if 
            //the input device doesn't have the required data available
            line.read(_internalBuffer, 0, internalBufferSize)
            _internalBufferPos = 0
        }
        
        //read and convert the next sample from our 
        //temp buffer and write it to the _sampleArray
        AudioIO.copyByteArrayToSignalSample(
                _internalBuffer, _internalBufferPos, 
                _sampleArray, 
                frameSize, audioFormat.isBigEndian())
        _internalBufferPos += frameSize
        
        //Write the value of _sampleArray to the output audioSignal
        audioSignal.write(_sampleArray) 
    }
}