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
        val audioOut = new OutputDevice(blockOnOutputDevice = true)
        audioOut.setPreferredDeviceBufferSize(3000)
        audioOut.getDriverControl().get.selectFirstMatch{driver =>
            driver.getMixerInfo().toString().contains("Speakers")
        }
        audioOut.getLineControl().get.selectValueByIndex(0)
        val outputFormat = AudioIO.createAudioFormat(sampleRate, 2, 1, true)
        audioOut.getAudioFormatControl().get.setValue(outputFormat)
        
        //Wire the 3 components together
        freqSweep.signalOutput --> signalGen.frequencyControl
        signalGen.signalOutput --> audioOut.audioSignal
        
        val ac = new AudioConfiguration()
        ac.addComponent(freqSweep)
        ac.addComponent(signalGen)
        ac.addComponent(audioOut)
        
        ac.start()
    }
}