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

trait GainControl {
self:ComponentControls with ComponentOutputs =>
    
    val gainControl = new FloatControl(startValue = 1.0f)
    gainControl.name = "Gain"
    gainControl.description = "Amplifies the output by the specified amount."
        
    //Add the control to the component's list of controls
    this.addControl(gainControl)
    //Add the processor to the output post-processors
    this.addPostProcessor(signalProcessor)
    
    def setGain(gain:Float){ gainControl.setValue(gain) }
    def getGain():Float = { gainControl.getValue() }
    
    private def signalProcessor(origSignal:Float):Float = {
         origSignal * gainControl.getValue()
    }
}