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
package siggen
import org.instabetter.scalaudio.components.controls.FrequencyControl
import org.instabetter.scalaudio.components.controls.AmplitudeOffsetControl
import org.instabetter.scalaudio.components.controls.GainControl

abstract class SignalGenerator(val sampleRate:Float, cycleOffset:Float) 
	extends Component with ComponentOutputs with ComponentControls
	with FrequencyControl {
    private val inverseSampleRate = 1.0f / sampleRate
    
    private var _cycle = cycleOffset % 1.0f
    
    val signalOutput = new OutputSignal(1)
    addOutput(signalOutput)
    
    override protected def process():Unit = {
        val signal = signalFunc(_cycle)
        
        _cycle += getFrequency() * inverseSampleRate
        
        //Make sure cycle stays between 0 and 1.0
        if(_cycle > 1.0f)
            _cycle = _cycle % 1.0f
        
        outputs().foreach(_.write(signal))
    }
    
    protected def signalFunc(cyclePos:Float):Float
    
}
