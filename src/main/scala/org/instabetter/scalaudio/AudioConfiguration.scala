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

import components._

class AudioConfiguration {
	protected var _components:Vector[Component] = Vector()
	private var _calcStatistics:Boolean = false
	private var _started:Boolean = false
	private var _running:Boolean = false
	
	
	
	def addComponent(component:Component){
	    if(!_components.contains(component))
	        _components :+= component
	}
	
	def getComponents() = { _components }
	
	def start(){
	    if(!_started){
		    _components.foreach{comp =>
		        comp.start()
		    }
		    _started = true
	    }
	    else if(!_running){
	        _components.foreach{comp =>
		        comp.unpause()
		    }
	    }
	    _running = true
	    
	    while(_running){
	        _components.foreach{comp=>
		        comp.processSignal()
		    }
		    
		    _components.foreach{comp=>
		        comp.propogateSignal()
		    }
	    }
	    
	    _components.foreach{comp =>
		    comp.pause()
		}
	    
	    if(_started){
	        _components.foreach{comp =>
		        comp.stop()
		    }
	    }
	}
	
	def pause(){
	    _running = false
	}
	
	def stop(){
	    if(_started && !_running){
	        _components.foreach{comp =>
		        comp.stop()
		    }
	    }
	    
	    _running = false
	    _started = false
	}
}