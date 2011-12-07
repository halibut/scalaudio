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
	protected var _jsMixer:Option[Mixer] = None
	protected var _jsDataLine:Option[DL] = None
	protected var _jsAudioFormat:Option[AudioFormat] = None
	protected var _preferredDeviceBufferSize:Int = 6000
	
	private val _lineListener = new LineListener{
	    override def update(lineEvent:LineEvent){
	        lineEventHandler(lineEvent)
	    }
	} 

	/**
	 * Returns a Seq of available Drivers (Mixers) for the AudioDevice
	 */
	def getAvailableDrivers():Seq[Mixer]
	
	/**
	 * Sets the Driver (Mixer) that the device will use
	 */
	def setDriver(driver:Mixer){
	    _jsMixer.foreach{ existingMixer => removeLine() }
	    _jsMixer = Option(driver)
	}
	
	/**
	 * Return the configured driver (mixer)
	 */
	def getDriver():Option[Mixer] = { _jsMixer }
	
	/**
	 * Returns a Seq of lines that are available from the selected Driver.
	 */
	def getAvailabileLines():Seq[DL] = {
	    if(_jsMixer.isEmpty){
	        throw new IllegalStateException("You must select a Driver (Mixer) before getting available lines.")
	    }
	    
	    internalGetDeviceLines()
	}
	
	/**
	 * Subclasses should implement this method which returns all
	 * the lines for the selected mixer. 
	 */
	protected def internalGetDeviceLines():Seq[DL]
	
	/**
	 * Set the line that will be used by the AudioDevice
	 * Note: A Driver must already be set, and the specified line must
	 * be available from the driver (Mixer). To get a list of 
	 * compatible lines @see getAvailabileLines
	 * @throws IllegalArgumentException if the line is not compatible with the 
	 * configured driver
	 */
	def setLine(dataLine:DL){
	    val mixer = _jsMixer.getOrElse{
	        throw new IllegalStateException("You must select a Driver (Mixer) before setting a line.")
	    }
	    if(!AudioDevice.lineIsFromDriver(dataLine, mixer)){
	        throw new IllegalArgumentException("The specified line is not available from the specified driver.")
	    }
	    removeLine()
	    _jsDataLine = Option(dataLine)
	    dataLine.addLineListener(_lineListener)
	}
	
	/**
	 * Returns a Seq of AudioFormat objects that are compatible with
	 * the configured line.
	 * @throws IllegalStateException if there is no configured line
	 */
	def getAvailableAudioFormats():Seq[AudioFormat] = {
	    val line = _jsDataLine.getOrElse{
	        throw new IllegalStateException("You must set a line before trying to retrieve AudioFormats.")
	    }
	    line.getLineInfo().asInstanceOf[DataLine.Info].getFormats()
	}
	
	/**
	 * Set the specified AudioFormat for the configured line
	 * @throws IllegalStateException if there is no configured line.
	 * @throws IllegalArgumentException if the audioFormat is not compatible 
	 * with the configured line, or if the AudioFormat has an unknown
	 * sampleRate, or number of channels. 
	 */
	def setAudioFormat(audioFormat:AudioFormat){
	    val line = _jsDataLine.getOrElse{
	        throw new IllegalStateException("You must hava a line configured before setting the AudioFormat.")
	    }
	    if(audioFormat.getChannels() == AudioSystem.NOT_SPECIFIED ||
	            audioFormat.getSampleRate() == AudioSystem.NOT_SPECIFIED){
	        throw new IllegalArgumentException("You must specify the number of channels and sample rate.")
	    }
	    if(!line.getLineInfo().asInstanceOf[DataLine.Info].isFormatSupported(audioFormat)){
	        throw new IllegalArgumentException("Not a compatible AudioFormat.")
	    }
	    _jsAudioFormat = Some(audioFormat)
	    audioFormatChanged()
	}
	
	private def removeLine(){
	    stopLine()
	    closeLine()
	    _jsAudioFormat = None
	    _jsDataLine.foreach{ line => line.removeLineListener(_lineListener) }
	    _jsDataLine = None
	}
	
	/**
	 * Open the line with the configured AudioFormat. The line will be in the stopped state.
	 * If the line was previously opened or started, it will be stopped and closed before re-opening.
	 * @throws IllegalStateException if an AudioFormat or a line hasn't been set.
	 */
	def openLine(){
	    stopLine()
	    closeLine()
	    val audioFormat = _jsAudioFormat.getOrElse{
	        throw new IllegalStateException("You must set an AudioFormat before you can open the line.")
	    }
	    val line = _jsDataLine.getOrElse{
	        throw new IllegalStateException("A line must be set before you can open it.")
	    }
	    internalOpenLine()
	}
	
	/**
	 * Opens the line with the specified audio format and any other arguments that
	 * need to be passed in while opening
	 */
	protected def internalOpenLine();
	
	/**
	 * Starts the line. Does nothing if no line is configured.
	 */
	def startLine(){ _jsDataLine.foreach{line => line.start()} }
	
	/**
	 * Stops the line. Does nothing if no line is configured.
	 */
	def stopLine(){ _jsDataLine.foreach{line => line.stop()} }
	
	/**
	 * Closes the line. Does nothing if no line is configured.
	 */
	def closeLine(){ _jsDataLine.foreach{line => line.close()} }
	
	/**
	 * Set the preferred number of samples for the device to buffer.
	 */
	def setPreferredDeviceBufferSize(bufferSizeInSamples:Int){
	    _preferredDeviceBufferSize = bufferSizeInSamples
	}
	
	protected def lineEventHandler(lineEvent:LineEvent){}
	
	protected def audioFormatChanged(){}
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
    def getInputDrivers():Seq[Mixer] = {
        if(_inputDevices.isEmpty)
            refreshDevicesList()
        _inputDevices.get.keySet.map{mixerInfo =>
            AudioSystem.getMixer(mixerInfo)
        }.toSeq
    }
    
    /**
     * Get the list of output drivers (Mixers with output lines)
     */
    def getOutputDrivers():Seq[Mixer] = {
        if(_outputDevices.isEmpty)
            refreshDevicesList()
        _outputDevices.get.keySet.map{mixerInfo =>
            AudioSystem.getMixer(mixerInfo)
        }.toSeq
    }
    
    /**
     * Get the input devices available for the specified driver.
     */
    def getInputDevices(driver:Mixer):Option[Seq[TargetDataLine]] = {
        val devicesOption = _inputDevices.get.get(driver.getMixerInfo())
        devicesOption.map{ devices => 
            devices.map{ lineInfo => 
                driver.getLine(lineInfo).asInstanceOf[TargetDataLine]
            }.toSeq 
        }
    }
    
    /**
     * Get the output devices available for the specified driver.
     */
    def getOutputDevices(driver:Mixer):Option[Seq[SourceDataLine]] = {
        val devicesOption = _outputDevices.get.get(driver.getMixerInfo())
        devicesOption.map{ devices => 
            devices.map{ lineInfo => 
                driver.getLine(lineInfo).asInstanceOf[SourceDataLine]
            }.toSeq 
        }
    }
    
    def lineIsFromDriver(line:DataLine,driver:Mixer):Boolean = {
        driver.isLineSupported(line.getLineInfo())
    }
}