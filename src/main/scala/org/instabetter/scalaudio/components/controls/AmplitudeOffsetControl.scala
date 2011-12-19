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

trait AmplitudeOffsetControl {
self:Component with ComponentControls with ComponentOutputs =>
    
    val amplitudeOffsetControl = new FloatControl(this)
    amplitudeOffsetControl.name = "Amplitude Offset"
    amplitudeOffsetControl.description = "Adds the specified offset to the signal."
    amplitudeOffsetControl.setValue(0.0f)
    
    //Add the control to the list of controls
    this.addControl(amplitudeOffsetControl)
    //Add the post processor to the output post processors
    this.addPostProcessor(signalProcessor)
    
    def setAmplitudeOffset(gain:Float){ amplitudeOffsetControl.setValue(gain) }
    def getAmplitudeOffset():Float = { amplitudeOffsetControl.getValue() }
    
    private def signalProcessor(origValue:Float):Float = {
        origValue + amplitudeOffsetControl.getValue()
    }
}