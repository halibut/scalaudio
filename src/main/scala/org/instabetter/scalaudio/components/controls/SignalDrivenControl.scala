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
package controls

class SignalDrivenControl(val control:FloatControl) extends Signal(1) {
	name = control.name
    description = control.description
    
    
    override def write(signalValue:Float){
        control.setValue(signalValue)
    }
    
    override def write(channelValues:Array[Float]){
        control.setValue(channelValues(0))
    }
    
    override def read():Array[Float] = {
        Array(control.getValue())
    }
    
    override def updateFrom(otherSignal:Signal){
        control.setValue(otherSignal.readAt(0))
    }
    
    override def combineWith(otherSignal:Signal)(combineFunc:(Float,Float)=>Float){
        throw new UnsupportedOperationException("Not a valid method for a SignalDrivenControl.")
    }
    
    override def modify(modifyFunc:(Float)=>Float){
        throw new UnsupportedOperationException("Not a valid method for a SignalDrivenControl.")
    }
}