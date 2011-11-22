package org.instabetter.scalaudio
package components
package io


class AudioIn(implicit sp:SignalProperties) extends Component(sp) {
	val inputs = new NoLineIOModule[Signal]()
    val outputs = new SingleLineIOModule[Signal]()
    val controls = new NoLineIOModule[Control]()
    
    
    
    def step(): Unit = {}
}