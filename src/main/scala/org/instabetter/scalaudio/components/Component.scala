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

abstract class Component(val signalProperties:SignalProperties) extends Identity{
    
    protected def preProcess():Unit = {};
    protected def process():Unit;
    protected def postProcess():Unit = {};
    
    def processSignal(){
        preProcess()
        process()
        postProcess()
    }
    
    def propogateSignal(){ }
}

trait ComponentControls {
self:Component =>

    private var _controls:Vector[Control] = Vector()
        
    def controls():IndexedSeq[Control] = _controls
    
    protected def addControl(control:Control){ _controls :+= control }
    protected def removeControl(control:Control){ _controls = _controls.filterNot(_ eq control) }
    protected def removeControl(index:Int){ removeControl(_controls(index)) }
    protected def removeControl(name:String){ _controls = _controls.filterNot(_.name eq name) }
}

