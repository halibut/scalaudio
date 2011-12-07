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


object JSoundTest2 {
    
    def main(args:Array[String]){
        
        val sampleRate = 44100f
        
        //Define the frequency sweep signal generator
        //Sweeps between 250-350 Hz every 1/5 second
        val freqSweep = new SineWaveGenerator(sampleRate) with SignalOutputControls
        freqSweep.setFrequency(.25f)
        freqSweep.setGain(100.0f)
        freqSweep.setAmplitudeOffset(150.0f)
        
        //Define the audio signal generator
        val signalGen = new SquareWaveGenerator(sampleRate)
        
        //Define the output device
        val audioOut = new OutputDevice()
        audioOut.setPreferredDeviceBufferSize(3000)
        val outputDriver = audioOut.getAvailableDrivers().find{ driver =>
            driver.getMixerInfo().toString().contains("Speakers")
        }.get
        audioOut.setDriver(outputDriver)
        val outputLine = audioOut.getAvailabileLines().head
        audioOut.setLine(outputLine)
        val outputFormat = AudioIO.createAudioFormat(sampleRate, 2, 1, true)
        audioOut.setAudioFormat(outputFormat)
        audioOut.openLine()
        audioOut.startLine()
        
        //Wire the 3 components together
        freqSweep.signalOutput --> signalGen.frequencyControl
        signalGen.signalOutput --> audioOut.audioSignal
        
        val components = List(freqSweep, signalGen, audioOut)
        
        
        val startTime = System.nanoTime()
        var curTime = startTime
        var curSample = 0L
        var printSamples = 0L
        
        var bufferedSamples = 0L
        val minBuffer = 441
        val sleepTime = (10).asInstanceOf[Int] 
        
        var lastSkippedSamples = 0L
        
        var printTime = 0L 
        while(true){
            val lastTime = curTime
            curTime = System.nanoTime()
            val elapsedNanos = curTime - startTime
            val elapsedSeconds = (0.000000001 * elapsedNanos)
            
            val lastSamples = curSample
            val elapsedSamples = (sampleRate * elapsedSeconds).asInstanceOf[Long]
            
            val samplesSinceWait = elapsedSamples - lastSamples
            curSample = elapsedSamples
            
            val samplesToWrite = samplesSinceWait + math.max(0, minBuffer - bufferedSamples)
            bufferedSamples += samplesToWrite - samplesSinceWait
            
            //Process the signal
            var loops = 0
            while(loops < samplesToWrite){
	            components.foreach(_.processSignal())
	            components.foreach(_.propogateSignal())
	            loops+=1
            }
            
            
            printSamples += samplesToWrite
            if(printSamples >= (sampleRate.asInstanceOf[Int]/2)){
                val totalSkippedSamples = audioOut.getSkippedSamples()
                val skippedSamples = totalSkippedSamples - lastSkippedSamples
                lastSkippedSamples = totalSkippedSamples
                
                val avgProcessTime = 0.000001 * printTime / printSamples
                
                println("Samples written: " + printSamples 
                        + "   Samples skipped: " + skippedSamples 
                        + "   Seconds: " + elapsedSeconds
                		+ "   Avg Process Time: " + avgProcessTime +" ms")
                println(signalGen.getFrequency() + " Hz   Samples: " + printSamples + "   Seconds: " + elapsedSeconds)
                printSamples -= (sampleRate.asInstanceOf[Int]/2).asInstanceOf[Long]
                printTime = 0L
            }
            
            val endTime = System.nanoTime()
            val diffTime = endTime - curTime
            printTime += diffTime
            val processTime = (0.000000001 * (diffTime)).asInstanceOf[Int]
            val adjustedSleepTime = sleepTime - processTime
            
            if(adjustedSleepTime > 0){
            	Thread.sleep(adjustedSleepTime)
            }
        }
    }
    
}