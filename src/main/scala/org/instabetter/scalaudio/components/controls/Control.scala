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

abstract class Control(val component:Component) extends ComponentPort with Identity{

    name = "Control"
    description = "Controls the function of a Component"
        
    def isConfigured():Boolean
}

class FloatControl(
        component:Component,
        val min:Float = Float.MinValue, 
        val max:Float = Float.MaxValue,
        startValue:Float = 0.0f) extends Control(component) with ConnectableFrom[Float,Array[Float]]{
    
    require(min <= max, "Min value must be less than max value.")
    
    val connectionOwner = component
    
    override def setValue(value:Float) { super.setValue(math.max(min, math.min(value, max))) }
    
    override def isConfigured():Boolean = true
    
    override def convert(sigVal:Array[Float]):Float = {
        sigVal(0)
    }
    
    override def getDefaultValue():Float = startValue
}

class EnumControl[T](component:Component, val options:IndexedSeq[T]) extends Control(component){
    private var _controlValue:Option[T] = None 
    
    def getValue():Option[T] = { _controlValue }
    def setValue(value:T) { 
        if(options.exists(enumsEqual(_,value))){
            _controlValue = Some(value) 
        } 
        onChangeValue(value)
    }
    
    def selectValueByIndex(index:Int) = {
        val value = options(index)
        setValue(value)
    }
    
    def selectFirstMatch(matchFunc:(T)=>Boolean) = {
        val selectedOpt = options.find(matchFunc)
        selectedOpt.foreach(setValue(_))
    }
    
    def onChangeValue(newValue:T){ }
    
    def display(enum:T):String = {
        enum.toString()
    }
    
    def enumsEqual(option:T, selectedValue:T):Boolean ={
        option == selectedValue
    }
    
    override def isConfigured():Boolean = {
        _controlValue.isDefined
    }
}