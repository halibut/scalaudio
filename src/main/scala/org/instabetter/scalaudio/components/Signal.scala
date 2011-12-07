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
import scala.collection.mutable.ArrayBuffer

class Signal(numChannels:Int = 1) extends Line{
    require(numChannels > 0, "A signal must contain 1 or more channels")
    
    private var _channels = new Array[Float](numChannels)
    private val _propertyChangeListeners:ArrayBuffer[Function1[Signal,Unit]] = ArrayBuffer()
    
    def setNumChannels(numChannels:Int){
        require(numChannels > 0, "A signal must contain 1 or more channels")
        if(numChannels != _channels.size ){
            _channels = new Array[Float](numChannels)
            for(listener <- _propertyChangeListeners){
                listener(this)
            }
        }
    }
    
    def channels():Int = { _channels.size }
    
    def addPropertyChangeListener(listener:(Signal)=>Unit){
        _propertyChangeListeners += listener
    }
    def removePropertyChangeListener(listener:(Signal)=>Unit){
        _propertyChangeListeners -= listener
    }
    
    def write(signalValue:Float){
        var i = 0
        while(i < _channels.size){
            _channels(i) = signalValue
            i+=1
        }
    }
    
    def write(channelValues:Array[Float]){
        Array.copy(channelValues, 0, _channels, 0, _channels.size)
    }
    
    def read():Array[Float] = {
        _channels.clone()
    }
    
    def readAt(index:Int):Float = {
        _channels(index)
    }
    
    def updateFrom(otherSignal:Signal){
        Array.copy(otherSignal._channels, 0, _channels, 0, _channels.size)
    }

    def combineWith(otherSignal:Signal)(combineFunc:(Float,Float)=>Float){
        var i = 0
        while(i < _channels.size){
            _channels(i) = combineFunc(_channels(i), otherSignal._channels(i))
            i+=1
        }
    }
    
    def modify(modifyFunc:(Float)=>Float){
        var i = 0
        while(i < _channels.size){
            _channels(i) = modifyFunc(_channels(i))
            i+=1
        }
    }
}