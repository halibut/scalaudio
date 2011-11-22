package org.instabetter.scalaudio
package components

class Channel extends Line{
    name = "Channel"
    description = "Carries a single channel of information in a signal."
	
    private var _value:Float = 0f

    def write(value: Float): Unit = { _value = value}

    def read(): Float = { _value }
}