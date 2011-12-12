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
import javax.sound.sampled.AudioFormat


object JSoundTest4 {
    
    def main(args:Array[String]){
        
        val sampleRate = 44100f
        val bytesPerSample = 2
        
        //Define the input device
        val audioIn = new InputDevice()
        audioIn.setPreferredDeviceBufferSize(3000)
        audioIn.getDriverControl().get.selectFirstMatch{ driver =>
            driver.getMixerInfo().toString().contains("Microphone")
        }
        audioIn.getLineControl().get.selectValueByIndex(0)
        val inputFormat = AudioIO.createAudioFormat(sampleRate, bytesPerSample, 1, true)
        audioIn.getAudioFormatControl().get.setValue(inputFormat)
        
        //Define the output device
        val audioOut = new OutputDevice()
        audioOut.setPreferredDeviceBufferSize(3000)
        //Select a driver for the speakers
        audioOut.getDriverControl().get.selectFirstMatch{driver =>
            driver.getMixerInfo().toString().contains("Speakers")
        }
        audioOut.getLineControl().get.selectValueByIndex(0)
        val outputFormat = AudioIO.createAudioFormat(sampleRate, bytesPerSample, 1, true)
        audioOut.getAudioFormatControl().get.setValue(outputFormat)
        
        //Wire the 2 components together
        audioIn.audioSignal --> audioOut.audioSignal
        
        
        val ac = new AudioConfiguration
        ac.addComponent(audioIn)
        ac.addComponent(audioOut)
        val components = List(audioIn, audioOut)
        
        ac.start()
    }
    
}