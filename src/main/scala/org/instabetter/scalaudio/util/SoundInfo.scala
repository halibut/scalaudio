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

package org.instabetter.scalaudio.util

import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Mixer
import javax.sound.sampled.Line
import javax.sound.sampled.Control
import javax.sound.sampled.BooleanControl
import javax.sound.sampled.FloatControl
import javax.sound.sampled.EnumControl
import javax.sound.sampled.CompoundControl
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine
import javax.sound.sampled.Port

object SoundInfo {

    def main(args:Array[String]){
        
        println("OS: "+System.getProperty("os.name")
                +" "+System.getProperty("os.version")
                +"/"+System.getProperty("os.arch"))
        println("Java: "+System.getProperty("java.version")
                +" ("+System.getProperty("java.vendor")+")");
        
	    AudioSystem.getMixerInfo().foreach{mixerInfo =>
	        printMixerInfo(mixerInfo)
	    }
    }
    
    private def printMixerInfo(mi:Mixer.Info){
        println("Audio Device: "+mi.getName())
        println("  Version: "+mi.getVersion())
        println("  Vendor: "+mi.getVendor())
        println("  Details: "+mi.getDescription())
        
        val mixer = AudioSystem.getMixer(mi)
        val wasOpened = !mixer.isOpen()
//        if(wasOpened)
//            mixer.open()
        
        val targets = mixer.getTargetLineInfo()
        val sources = mixer.getSourceLineInfo()
        val controls = mixer.getControls()
        
        
        if(!targets.isEmpty){
            println("    Target Lines: ")
            targets.foreach{lineInfo => printLineInfo("      ", lineInfo, mixer)}
        }
        if(!sources.isEmpty){
        	println("    Source Lines: ")
        	sources.foreach{lineInfo => printLineInfo("      ", lineInfo, mixer)}
        }
        if(!controls.isEmpty){
            println("    Controls: ")
            controls.foreach(control=> printControlInfo("      ", control))
        }
        
//        if(wasOpened)
//            mixer.close()
    }
    
    private def printLineInfo(prefix:String, lInfo:Line.Info, mixer:Mixer){
        lInfo match {
            case port:Port.Info =>
                println(prefix+"Port: "+port.getName())
                val line = mixer.getLine(port)
                line.open()
                val portControls = line.getControls()
                if(!portControls.isEmpty){
                    println(prefix+"  Controls:")
                    portControls.foreach(control=> printControlInfo(prefix+"    ", control))
                }
                line.close()
            case info =>
                println(prefix+"Line: "+info)
        }
    }
    
    private def printControlInfo(prefix:String, control:Control){
        control match {
            case bc:BooleanControl =>
                println(prefix+bc.getType()+" (On/Off)")
            case fc:FloatControl =>
                println(prefix+fc.getType()+" ("+fc.getMinimum()+" to "+fc.getMaximum()+")")
            case ec:EnumControl =>
                println(prefix+ec.getType()+" (Values: "+ec.getValues().mkString+")")
            case cc:CompoundControl =>
                println(prefix+cc.getType()+" (Compound):")
                cc.getMemberControls().foreach(printControlInfo(prefix+"  ",_))
            case c => 
                println(prefix+c.getType()+" (Unknown Type)")
        }
    }
}