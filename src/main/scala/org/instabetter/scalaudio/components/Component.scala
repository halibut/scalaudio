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

/**
 * A component is a functional unit that takes input signals, processes
 * them, and sends the resulting signals to the outputs. It is the 
 * fundamental building block of the signal manipulation program.
 */
trait Component extends Identity{
    
    protected def preProcess():Unit = {};
    protected def process():Unit;
    protected def postProcess():Unit = {};
    
    /**
     * Implementations that need to do something special before it can star working
     * should override this method. It will be called whenever an AudioConfiguration is started. 
     */
    def start():Unit = {};
    /**
     * Implementations that need to do something special after it stops running (cleanup, 
     * closing a device, etc... It will be called whenever an AudioConfiguration is stopped. 
     */
    def stop():Unit = {};
    
    /**
     * Implementations that need to do something special after it has paused (temporary stop).
     * The component should be in a state that in can be unpaused and continue working. 
     * It will be called whenever an AudioConfiguration is paused. 
     */
    def pause():Unit = { };
    
    /**
     * Implementations that need to do something special after it has unpaused (resume from
     * a temporary stop). The component should be in a working (running) state. 
     * It will be called whenever an AudioConfiguration is unpaused (resumed). 
     */
    def unpause():Unit = { };
    
    /**
     * Causes the component to read its inputs, and process them.
     */
    def processSignal(){
        preProcess()
        process()
        postProcess()
    }
    
    /**
     * Causes the outputs of the signal to propogate to other connected
     * components.
     */
    def propogateSignal(){ }
}

trait ComponentControls {
self:Component =>

    private var _controls:Vector[Control] = Vector()
        
    def controls():IndexedSeq[Control] = _controls
    
    protected def addControl(control:Control){ _controls :+= control }
    protected def removeControl(control:Control){ _controls = _controls.filterNot(_ == control) }
    protected def removeControl(index:Int){ removeControl(_controls(index)) }
    protected def removeControl(name:String){ _controls = _controls.filterNot(_.name == name) }
    
    protected def getControl(name:String):Option[Control] = { _controls.find(_.name == name)}
}

