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

trait ComponentInputs {
self:Component =>
	
    private var _signalPreProcessors:Vector[Function1[Float,Float]] = Vector()
    private var _inputs:Vector[InputSignal] = Vector()
        
    def inputs:IndexedSeq[InputSignal] = _inputs
    
    protected def addInput(input:InputSignal){ _inputs :+= input }
    protected def removeInput(input:InputSignal){ _inputs = _inputs.filterNot(_ eq input) }
    protected def removeInput(index:Int){ removeInput(_inputs(index)) }
    protected def removeInput(name:String){ _inputs = _inputs.filterNot(_.name eq name) }
    
    def addPreProcessor(func:(Float)=>Float) = { 
        _signalPreProcessors :+= func 
    }
    def removePreProcessor(func:(Float)=>Float) = {
        _signalPreProcessors = _signalPreProcessors.filterNot(_ eq func)
    }
    
    override def preProcess(){
        _inputs.foreach{input=>
            _signalPreProcessors.foreach{processor=>
            	input.modify(processor)
            }
        }
    }
}
