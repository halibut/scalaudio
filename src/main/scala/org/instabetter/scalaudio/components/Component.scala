package org.instabetter.scalaudio
package components

abstract class Component(val signalProperties:SignalProperties) extends Identity{
    val inputs:ComponentIOModule[Signal];
    val outputs:ComponentIOModule[Signal];
    val controls:ComponentIOModule[Control];
    
    def step():Unit;
}