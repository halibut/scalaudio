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

import controls._

class OutputSignal(owner:Component, numChannels:Int = 1) extends ConnectableTo[Array[Float]] with Signal{
    val portOwner = owner
    
    def wireTo(signal:InputSignal){ 
        signal.connectFrom(this)
    }
    def wireTo(control:FloatControl){ 
        control.connectFrom(this)
    }
    def -->(signal:InputSignal){ wireTo(signal) }
    def -->(control:FloatControl){ wireTo(control) }

    override protected def getDefaultValue():Array[Float] = {
        new Array[Float](numChannels)
    }
    
    override def setNumChannels(numChannels:Int){
        if(numChannels != channels()){
	        for(connection <- getConnectedTo){
	            connection.disconnect()
	        }
        }
        setValue(new Array[Float](numChannels))
    }
}