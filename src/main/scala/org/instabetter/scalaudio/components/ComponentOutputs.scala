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

trait ComponentOutputs {
self:Component =>

    private var _signalPostProcessors:Vector[Function1[Float,Float]] = Vector()
    private var _outputs:Vector[OutputSignal] = Vector()
        
    def outputs:IndexedSeq[OutputSignal] = _outputs
    
    protected def addOutput(output:OutputSignal){ _outputs :+= output }
    protected def removeOutput(output:OutputSignal){ _outputs = _outputs.filterNot(_ eq output) }
    protected def removeOutput(index:Int){ removeOutput(_outputs(index)) }
    protected def removeOutput(name:String){ _outputs = _outputs.filterNot(_.name eq name) }
    
    def addPostProcessor(func:(Float)=>Float) = { 
        _signalPostProcessors :+= func 
    }
    def removePostProcessor(func:(Float)=>Float) = {
        _signalPostProcessors = _signalPostProcessors.filterNot(_ eq func)
    }
    
    override def postProcess(){
        _outputs.foreach{output=>
            _signalPostProcessors.foreach{processor=>
            	output.modify(processor)
            }
        }
    }
    
    override def propogateSignal(){
        _outputs.foreach( output => output.propogateValue())
    }
}