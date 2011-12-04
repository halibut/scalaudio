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

abstract class Control extends Identity{

    name = "Control"
    description = "Controls the function of a Component"
}

class FloatControl(
        val min:Float = Float.MinValue, 
        val max:Float = Float.MaxValue,
        startValue:Float = 0.0f) extends Control{
    
    require(min <= max, "Min value must be less than max value.")
    
    private var _controlValue:Float = math.max(min, math.min(startValue, max))
    
    def getValue():Float = { _controlValue }
    def setValue(value:Float) { _controlValue = math.max(min, math.min(value, max)) }
}