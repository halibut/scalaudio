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

class Signal(val numChannels:Int = 1) extends Line{
    require(numChannels > 0, "A signal must contain 1 or more channels")
    
    private val channels = new Array[Float](numChannels)
    
    def write(signalValue:Float){
        var i = 0
        while(i < numChannels){
            channels(i) = signalValue
            i+=1
        }
    }
    
    def write(channelValues:Array[Float]){
        Array.copy(channelValues, 0, channels, 0, numChannels)
    }
    
    def read():Array[Float] = {
        channels.clone()
    }
    
    def readAt(index:Int):Float = {
        channels(index)
    }
    
    def updateFrom(otherSignal:Signal){
        Array.copy(otherSignal.channels, 0, channels, 0, numChannels)
    }

    def combineWith(otherSignal:Signal)(combineFunc:(Float,Float)=>Float){
        var i = 0
        while(i < numChannels){
            channels(i) = combineFunc(channels(i), otherSignal.channels(i))
            i+=1
        }
    }
    
    def modify(modifyFunc:(Float)=>Float){
        var i = 0
        while(i < numChannels){
            channels(i) = modifyFunc(channels(i))
            i+=1
        }
    }
}