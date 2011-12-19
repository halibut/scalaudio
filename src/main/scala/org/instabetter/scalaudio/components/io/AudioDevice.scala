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

import components.controls._
import javax.sound.sampled.Mixer
import javax.sound.sampled.DataLine
import javax.sound.sampled.AudioSystem
import scala.collection.mutable.ArrayBuffer
import javax.sound.sampled.TargetDataLine
import javax.sound.sampled.SourceDataLine
import scala.collection.mutable.Map
import javax.sound.sampled.LineListener
import javax.sound.sampled.LineEvent
import javax.sound.sampled.AudioFormat

trait AudioDevice[DL <: DataLine]  {
self:Component with ComponentControls =>
	protected var _preferredDeviceBufferSize:Int = 6000
	
	private val _lineListener = new LineListener{
	    override def update(lineEvent:LineEvent){
	        lineEventHandler(lineEvent)
	    }
	} 

	private val audioDevice = this
	
	//Setup controls for selecting driver, line, and audio format
	{
	    //Selection for the driver
	    val driverControl = new EnumControl[Mixer](audioDevice, getAvailableDrivers()){
	        name = "Driver"
	        description = "Select the driver for this AudioDevice"
	        private var lineControl:EnumControl[DL] = null
	        override def onChangeValue(driver:Mixer){
	            stop()
	            if(lineControl != null){
	                removeControl(lineControl)
	            }
	            
	            //Selection for the Line
	            lineControl = new EnumControl[DL](audioDevice, internalGetDeviceLines()){
	                name = "Line"
	                description = "Select the Line for this AudioDevice"
	                private var audioFormatControl:EnumControl[AudioFormat] = null
	                override def onChangeValue(line:DL){
	                    stop()
	                    if(audioFormatControl != null){
	                        removeControl(audioFormatControl)
	                    }
	                    
	                    //Selection for the AudioFormat
	                    audioFormatControl = new EnumControl(audioDevice, getAvailableAudioFormats()){
	                        name = "Audio Format"
	                        description = "Select the audio format for this AudioDevice"
	                            
	                        //Override equals function to disallow NOT_SPECIFIED values for anything
	                        override def enumsEqual(option:AudioFormat, test:AudioFormat):Boolean = {
	                            val baseMatches = 
	                                getLine().get.getLineInfo().asInstanceOf[DataLine.Info].isFormatSupported(test)
	                            val found = baseMatches &&
	                            	test.getChannels() != AudioSystem.NOT_SPECIFIED &&
	                            	test.getEncoding() == AudioFormat.Encoding.PCM_SIGNED &&
	                            	test.getFrameRate() != AudioSystem.NOT_SPECIFIED &&
	                            	test.getSampleRate() != AudioSystem.NOT_SPECIFIED &&
	                            	test.getFrameSize() != AudioSystem.NOT_SPECIFIED &&
	                            	test.getSampleSizeInBits() != AudioSystem.NOT_SPECIFIED
	                            
	                            found
	                        }
	                        
	                        override def onChangeValue(audioFormat:AudioFormat){
	                            audioFormatChanged()
	                        }
	                    }
	                    addControl(audioFormatControl)
	                }
	            }
	            addControl(lineControl)
	        }
	    }
	    addControl(driverControl)
	}
	
	/**
	 * Returns a Seq of available Drivers (Mixers) for the AudioDevice
	 */
	protected def getAvailableDrivers():IndexedSeq[Mixer]

		/**
	 * Subclasses should implement this method which returns all
	 * the lines for the selected mixer. 
	 */
	protected def internalGetDeviceLines():IndexedSeq[DL]

	/**
	 * Returns a Seq of AudioFormat objects that are compatible with
	 * the configured line.
	 * @throws IllegalStateException if there is no configured line
	 */
	protected def getAvailableAudioFormats():IndexedSeq[AudioFormat] = {
	    getLineControl.get.getValue.get.getLineInfo().asInstanceOf[DataLine.Info].getFormats()
	}
	
	/**
	 * Return the configured driver (mixer)
	 */
	def getDriverControl():Option[EnumControl[Mixer]] = { 
	    getControl("Driver").map(_.asInstanceOf[EnumControl[Mixer]]) 
	}
	
	protected def getDriver():Option[Mixer] = {
	    getDriverControl().flatMap(_.getValue())
	}
	
	/**
	 * Return the configured Line
	 */
	def getLineControl():Option[EnumControl[DL]] = { 
	    getControl("Line").map(_.asInstanceOf[EnumControl[DL]]) 
	}
	
	protected def getLine():Option[DL] = {
	    getLineControl().flatMap(_.getValue())
	}
	
	/**
	 * Return the configured driver AudioFormat
	 */
	def getAudioFormatControl():Option[EnumControl[AudioFormat]] = { 
	    getControl("Audio Format").map(_.asInstanceOf[EnumControl[AudioFormat]]) 
	}
	
	protected def getAudioFormat():Option[AudioFormat] = {
	    getAudioFormatControl().flatMap(_.getValue())
	}
	
	def setupDevice(driver:Mixer, line:DL, audioFormat:AudioFormat){
	    getDriverControl().get.setValue(driver)
	    getLineControl().get.setValue(line)
	    getAudioFormatControl().get.setValue(audioFormat)
	}
	
	/**
	 * Opens the line with the specified audio format and any other arguments that
	 * need to be passed in while opening
	 */
	protected def internalOpenLine();
	
	/**
	 * Set the preferred number of samples for the device to buffer.
	 */
	def setPreferredDeviceBufferSize(bufferSizeInSamples:Int){
	    _preferredDeviceBufferSize = bufferSizeInSamples
	}
	
	protected def lineEventHandler(lineEvent:LineEvent){}
	
	protected def audioFormatChanged(){}
	
	
	override def start(){
	    getLine().foreach{line =>
	        internalOpenLine()
	        unpause();
	    }
	}
	
	override def stop(){
	    getLine().foreach{line =>
	        pause();
	        line.close();
	    }
	}

}

object AudioDevice {
    private var _inputDevices:Option[Map[Mixer.Info,ArrayBuffer[DataLine.Info]]] = None
    private var _outputDevices:Option[Map[Mixer.Info,ArrayBuffer[DataLine.Info]]] = None
    
    /**
     * Refreshes the list of drivers and devices.
     */
    def refreshDevicesList(){
        val inputDevices = Map[Mixer.Info,ArrayBuffer[DataLine.Info]]()
        val outputDevices = Map[Mixer.Info,ArrayBuffer[DataLine.Info]]()
        
        val mixers = AudioSystem.getMixerInfo()
                .map(AudioSystem.getMixer(_))
                
        for(mixer <- mixers){
            val mixerInfo = mixer.getMixerInfo()
            val inputInfos = mixer.getTargetLineInfo()
            for(input <- inputInfos){
                if(classOf[TargetDataLine].isAssignableFrom(input.getLineClass())){
                    val dataLineInfo = input.asInstanceOf[DataLine.Info]
                    var mixerLines = inputDevices.get(mixerInfo)
                    if(mixerLines.isEmpty){
                        mixerLines = Some(ArrayBuffer())
                        inputDevices += (mixerInfo -> mixerLines.get)
                    }
                    mixerLines.get += dataLineInfo 
                }
            }
            
            val outputInfos = mixer.getSourceLineInfo()
            for(output <- outputInfos){
                if(classOf[SourceDataLine].isAssignableFrom(output.getLineClass())){
                    val dataLineInfo = output.asInstanceOf[DataLine.Info]
                    var mixerLines = outputDevices.get(mixerInfo)
                    if(mixerLines.isEmpty){
                        mixerLines = Some(ArrayBuffer())
                        outputDevices += (mixerInfo -> mixerLines.get)
                    }
                    mixerLines.get += dataLineInfo 
                }
            }
        }

        
        
        _inputDevices = Some(inputDevices)
        _outputDevices = Some(outputDevices)
    }
    
    /**
     * Get the list of input drivers (Mixers with input lines)
     */
    def getInputDrivers():IndexedSeq[Mixer] = {
        if(_inputDevices.isEmpty)
            refreshDevicesList()
        _inputDevices.get.keySet.map{mixerInfo =>
            AudioSystem.getMixer(mixerInfo)
        }.toIndexedSeq
    }
    
    /**
     * Get the list of output drivers (Mixers with output lines)
     */
    def getOutputDrivers():IndexedSeq[Mixer] = {
        if(_outputDevices.isEmpty)
            refreshDevicesList()
        _outputDevices.get.keySet.map{mixerInfo =>
            AudioSystem.getMixer(mixerInfo)
        }.toIndexedSeq
    }
    
    /**
     * Get the input devices available for the specified driver.
     */
    def getInputDevices(driver:Mixer):Option[IndexedSeq[TargetDataLine]] = {
        val devicesOption = _inputDevices.get.get(driver.getMixerInfo())
        devicesOption.map{ devices => 
            devices.map{ lineInfo => 
                driver.getLine(lineInfo).asInstanceOf[TargetDataLine]
            }.toIndexedSeq 
        }
    }
    
    /**
     * Get the output devices available for the specified driver.
     */
    def getOutputDevices(driver:Mixer):Option[IndexedSeq[SourceDataLine]] = {
        val devicesOption = _outputDevices.get.get(driver.getMixerInfo())
        devicesOption.map{ devices => 
            devices.map{ lineInfo => 
                driver.getLine(lineInfo).asInstanceOf[SourceDataLine]
            }.toIndexedSeq 
        }
    }
    
    def lineIsFromDriver(line:DataLine,driver:Mixer):Boolean = {
        driver.isLineSupported(line.getLineInfo())
    }
}