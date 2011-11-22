package org.instabetter.scalaudio
package components
package convert

class SignalDrivenControl(val control:Control) extends Signal(Vector(control)) {
	name = control.name
    description = control.description
    
}