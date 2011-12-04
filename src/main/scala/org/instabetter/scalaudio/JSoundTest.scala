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

import javax.sound.sampled._
import com.sun.media.sound.DirectAudioDevice

object JSoundTest {
    
    def main(args:Array[String]){
    	
    	val simpleMixerInfo = AudioSystem.getMixerInfo().find{info =>
        	info.getClass.getName.contains("Direct")
    	}.get
    	val simpleMixer = AudioSystem.getMixer(simpleMixerInfo)
    	
    	
    	val sampleRate = 88200L
        
        val audioFormat = new AudioFormat(
                sampleRate.asInstanceOf[Float],
                16,
                1,
                true,
                true);
        
//        val targetLine:TargetDataLine = AudioSystem.getTargetDataLine(audioFormat, simpleMixerInfo)
//        println(targetLine.getLineInfo())
//        printLineInfo(targetLine,"  ");
//        targetLine.open(audioFormat);
//        targetLine.start();
        
        val targetLineInfo = new DataLine.Info ( classOf[SourceDataLine], audioFormat );
        val inputLine:SourceDataLine = AudioSystem.getLine(targetLineInfo).asInstanceOf[SourceDataLine];
        println(inputLine.getLineInfo())
        printLineInfo(inputLine,"  ");
        
        inputLine.open(audioFormat);
        inputLine.start();
        
        val minFreq = 20f;
        val maxFreq = 800f;
        val halfFreqRange = 0.5f * (maxFreq - minFreq);
        val midFreq = halfFreqRange + minFreq;
        
        
        val freqCycle = 50; //should adjust between min and max freq every 10 seconds
        
        var elapsedSamples = 0L;
        var elapsedSeconds = 0.0
        while(true){
            elapsedSeconds += 1.0 / sampleRate
            val cycle = 2.0 * math.Pi * elapsedSeconds
            val curFreq = 250 + (200 * math.cos(cycle / freqCycle))
            //val curFreq = 400
            //val decimal = elapsedSeconds - math.floor(elapsedSeconds)
            //val freqAdj = 100.0 * (if(decimal > 0.5)(decimal - 0.5) else -decimal) 
            //val curFreq = 400 + freqAdj
            
            val normalizedFreq = 0.01 * math.round(curFreq * 100.0)
            //val normalizedFreq = curFreq
            var majorSampleDouble = math.cos(cycle * normalizedFreq)
            var minorSampleDouble = math.cos((cycle+.333333) * normalizedFreq * 1.5)
            var tertiarySampleDouble = math.cos((cycle+.666666) * normalizedFreq * 1.75)
            //val sampleDouble = math.sin(2.0 * math.Pi * 400.0 * elapsedTime)
            
//            if(sampleDouble > 0.01)
//                sampleDouble = 1
//            else if(sampleDouble < - 0.01)
//                sampleDouble = -1
            
            val sampleDouble = majorSampleDouble * 0.8 + minorSampleDouble * 0.15 + tertiarySampleDouble * 0.05;
            val sampleShort = math.round(sampleDouble * Short.MaxValue).asInstanceOf[Short];
            
            inputLine.write(convertShortToByteArray(sampleShort), 0, 2)
            
            elapsedSamples+=1
            if(elapsedSamples < 0)
                println("OMG! OVERFLOW!")
            
//            println(elapsedSamples+","+sampleDouble)
            if(elapsedSamples % (sampleRate/8) == 0){
                println(curFreq)
                //inputLine.drain()
            }
        }
    }
    
    private def convertShortToByteArray(in:Short):Array[Byte] = {
        val byteArray = new Array[Byte](2)
        byteArray(0) = ((in & 0x0000FF00) >> 8).asInstanceOf[Byte]
        byteArray(1) = (in & 0x000000FF).asInstanceOf[Byte]
        
        return byteArray
    }
    
    private def printLineInfo(line:Line, indentation:String){
        if(!line.isInstanceOf[DataLine])
            return
            
        val dLine = line.asInstanceOf[DataLine]
        println(indentation+dLine)
        line.getLineInfo() match{
            case dlInfo:DataLine.Info =>
                println(indentation + "  Formats:")
                dlInfo.getFormats().foreach{format =>
                    
                    println(indentation + "    " + format)
                }
                
                println(indentation + "   Level: " + dLine.getLevel())
        }
    }

}