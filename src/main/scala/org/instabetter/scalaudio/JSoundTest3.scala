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


import components._
import components.io._
import components.siggen._
import components.controls._
import javax.sound.sampled.Mixer
import javax.sound.sampled.TargetDataLine
import javax.sound.sampled.SourceDataLine
import javax.sound.sampled.DataLine
import com.groovemanager.spi.asio.ASIOLineInfo
import com.groovemanager.spi.asio.ASIOMixer
import com.groovemanager.spi.asio.ASIODataLine


object JSoundTest3 {
    
    def main(args:Array[String]){
        
        val inBufferSize = 1500
        val outBufferSize = 1000
        
        val insp = SignalProperties(
                sampleRate = 44100f,
                bytesPerChannel = 4,
                maxDelaySeconds = .05f)
                
        val outsp = SignalProperties(
                sampleRate = 44100f,
                bytesPerChannel = 4,
                maxDelaySeconds = .05f)
    	
        val inaf = AudioIO.createAudioFormat(insp, 1, false)
        var inputLineInfo = AudioIO.createInputLineInfo(inaf, inBufferSize)
        
        val outaf = AudioIO.createAudioFormat(outsp, 2, false)
        var outputLineInfo = AudioIO.createOutputLineInfo(outaf, outBufferSize)
        
        var targetInputMixer:Mixer = null
        AudioIO.getCompatibleIODevices(inputLineInfo).foreach{mixer =>
            mixer.open()
            println(mixer.getMixerInfo() + " - " + mixer)
            if(mixer.getMixerInfo().toString().contains("ASIO") && targetInputMixer == null){
                targetInputMixer = mixer
                println(mixer.getMixerInfo())
                targetInputMixer.getTargetLineInfo().map{line => 
                    println("Line: " + line);
                    if(line.toString().contains("Microphone 1"))
                        inputLineInfo = line.asInstanceOf[DataLine.Info]
                }
            }
            mixer.close()
        }
        
        var targetOutputMixer:Mixer = null
        AudioIO.getCompatibleIODevices(outputLineInfo).foreach{mixer =>
            mixer.open()
            println(mixer.getMixerInfo() + " - " + mixer)
            if(mixer.getMixerInfo().toString().contains("ASIO") && targetOutputMixer == null){
                targetOutputMixer = mixer
                targetOutputMixer.getSourceLineInfo().map{line => 
                    println("Line: " + line);
                    if(line.toString().contains("HD Audio output 1"))
                        outputLineInfo = line.asInstanceOf[DataLine.Info]
                }
            }
            mixer.close()
        }
        
        
        val inputLine = targetInputMixer.getLine(inputLineInfo).asInstanceOf[TargetDataLine]
        val outputLine = targetOutputMixer.getLine(outputLineInfo).asInstanceOf[SourceDataLine]
        
        val allASIO = targetOutputMixer == targetInputMixer && targetOutputMixer.isInstanceOf[ASIOMixer] 
        if(allASIO){
        	targetInputMixer.synchronize(Array(inputLine, outputLine), true)
        	
        	val asioInLine = inputLine.asInstanceOf[ASIODataLine]
        	val asioOutLine = outputLine.asInstanceOf[ASIODataLine]
        	
        	asioInLine.setDesiredFormat(inaf, inBufferSize)
        	asioOutLine.setDesiredFormat(outaf, outBufferSize)
        	
        	asioInLine.open()
        	asioOutLine.open()
        }
        else{
            inputLine.open(inaf, inBufferSize)
	        outputLine.open(outaf, outBufferSize)
        }
        
        inputLine.start()
	    outputLine.start()
        
        val floatArray = new Array[Float](1)
        val inputBuffer = new Array[Byte](floatArray.size * inaf.getFrameSize() )
        val outputBuffer = new Array[Byte](floatArray.size * outaf.getFrameSize() )
        
        
//        val inputToOutputDelaySamples = 20 / floatArray.size
//        for(i <- 0 until inputToOutputDelaySamples){
//            outputLine.write(outputBuffer, 0, outputBuffer.size)
//        }
        
        while(true){
            
            inputLine.read(inputBuffer, 0, inputBuffer.size)
            
            if(allASIO){
                Array.copy(inputBuffer, 0, outputBuffer, 0, inputBuffer.size)
                
                AudioIO.copyByteArrayToSignalSample(
	                    inputBuffer, 0, floatArray, 4, false)
	                    
	            AudioIO.copySignalSampleToByteArray(
	                    floatArray, outputBuffer, 0, 4, false)
	            AudioIO.copySignalSampleToByteArray(
	                    floatArray, outputBuffer, 4, 4, false)
                
                outputLine.write(outputBuffer, 0, outputBuffer.size)
            }
            else{
	            AudioIO.copyByteArrayToSignalSample(
	                    inputBuffer, 0, floatArray, 4, false)
	                    
	            AudioIO.copySignalSampleToByteArray(
	                    floatArray, outputBuffer, 0, 4, false)
	                    
	            outputLine.write(outputBuffer, 0, outputBuffer.size)
	            outputLine.write(outputBuffer, 0, outputBuffer.size)
            }
        }
    }
    
}