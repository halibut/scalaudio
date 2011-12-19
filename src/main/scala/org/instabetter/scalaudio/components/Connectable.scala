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
import scala.annotation.implicitNotFound

trait Connectable[T] extends Identity{
    val connectionOwner:Component
    
    private var _value:T = getDefaultValue()
    
    protected def getDefaultValue():T;
    
    def getValue():T = { _value }
    
    def setValue(value:T){
        _value = value
    }
    
    def isConnected():Boolean;
    
    def isConnectable(o:Connectable[Any]):Boolean
}

trait ConnectableFrom[T,O] extends Connectable[T]{
	
    private var _connectableFrom:Option[ConnectableTo[O]] = None
    
    def connectFrom(from:ConnectableTo[O]){
        _connectableFrom.foreach{existingFrom =>
            if(!(existingFrom eq from)){
                disconnect
            }
        }
        _connectableFrom = Some(from)
        from.connectTo(this.asInstanceOf[ConnectableFrom[Any,O]])
    }
    
    def disconnect(){
        _connectableFrom.foreach{from =>
            from.disconnect(this.asInstanceOf[ConnectableFrom[Any,O]])
        }
        _connectableFrom = None
    }
    
    def getConnectedFrom():Option[ConnectableTo[O]] ={
        _connectableFrom
    }

    def convert(fromVal:O):T;
    
    def isConnected():Boolean = {
        _connectableFrom.isDefined
    }
    
    def isConnectable(o:Connectable[Any]):Boolean = {
        o.isInstanceOf[ConnectableTo[_]]
    }
}

trait ConnectableTo[T] extends Connectable[T]{
    private var _connectableTo:Set[ConnectableFrom[Any,T]] = Set();
    
    def connectTo(to:ConnectableFrom[Any,T]){
        _connectableTo += to
    }
    
    def disconnect(to:ConnectableFrom[Any,T]){
        _connectableTo -= to
    }
    
    def getConnectedTo():Set[ConnectableFrom[Any,T]] = {
        _connectableTo
    }
    
    def propogateValue(){
        val value = getValue()
        for(to <- _connectableTo){
            val convertedVal = to.convert(value)
            to.setValue(convertedVal)
        }
    }
       
    def isConnected():Boolean = {
        !_connectableTo.isEmpty
    }
    
    def isConnectable(o:Connectable[Any]):Boolean = {
        o.isInstanceOf[ConnectableFrom[_,_]]
    }
}
