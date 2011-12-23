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

class InputSignal(owner:Component, numChannels:Int = 1) extends ConnectableFrom[Array[Float],Array[Float]] with Signal{
    require(numChannels > 0, "A signal must contain 1 or more channels")
    
    val portOwner = owner
    
    def setNumChannels(numChannels:Int){
        require(numChannels > 0, "A signal must contain 1 or more channels")
        disconnect()
        setValue(new Array[Float](numChannels))
    }
    
    def convert(outSigVal:Array[Float]):Array[Float] = { outSigVal }
    
    override protected def getDefaultValue():Array[Float] = {
        new Array[Float](numChannels)
    }
    
//    def updateFrom(otherSignal:Signal){
//        Array.copy(otherSignal.getValue(), 0, getValue(), 0, getValue().size)
//    }
//
//    def combineWith(otherSignal:InputSignal)(combineFunc:(Float,Float)=>Float){
//        var i = 0
//        val array = getValue()
//        val oArray = otherSignal.getValue()
//        while(i < array.size){
//            array(i) = combineFunc(array(i), oArray(i))
//            i+=1
//        }
//    }
   

}