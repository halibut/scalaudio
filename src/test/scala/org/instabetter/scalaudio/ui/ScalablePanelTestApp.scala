package org.instabetter.scalaudio
package ui

import scala.swing.SimpleSwingApplication
import scala.swing.MainFrame
import scala.swing.Button
import scala.swing.Label
import scala.swing.ScrollPane
import java.awt.event.MouseWheelEvent
import scala.swing.event.MouseWheelMoved
import java.awt.Color
import components._
import components.controls._
import components.siggen._

object ScalablePanelTestApp extends SimpleSwingApplication {

    def top = new MainFrame{
        title = "Hello, World!"
		contents = new ScrollPane(
            new ScrollableScalablePanel {
	        	
	        	val audioComp1 = new DummyComponent() with ComponentInputs with ComponentOutputs with ComponentControls{
	        	    name = "Comp 1"
	        	    addInput(new InputSignal(this, 1))
	        	    addInput(new InputSignal(this, 1))
	        	    addInput(new InputSignal(this, 1))
	        	    addOutput(new OutputSignal(this, 1))
	        	    addOutput(new OutputSignal(this, 1))
	        	    addOutput(new OutputSignal(this, 1))
	        	    addControl(new FloatControl(this))
	        	}
	        	
	        	val audioComp2 = new DummyComponent() with ComponentInputs with ComponentOutputs{
	        	    name = "Component 2"
	        	    addInput(new InputSignal(this, 1))
	        	    addInput(new InputSignal(this, 1))
	        	    addInput(new InputSignal(this, 1))
	        	    addOutput(new OutputSignal(this, 1))
	        	}
	        	
	        	val audioComp3 = new DummyComponent() with ComponentInputs with ComponentControls{
	        	    name = "Comp 3"
	        	    addInput(new InputSignal(this, 1))
	        	    addInput(new InputSignal(this, 1))
	        	    addControl(new FloatControl(this))
	        	    addControl(new FloatControl(this))
	        	    addControl(new FloatControl(this))
	        	}
	        	
	        	val audioComp4 = new SawWaveGenerator(44100f)
	        	
	        	audioComp1.outputs(0) --> audioComp3.inputs(0)
	        	audioComp1.outputs(0) --> audioComp2.inputs(0)
	        	audioComp2.outputs(0) --> audioComp3.inputs(1)
	        	audioComp4.outputs(0) --> audioComp3.controls(2).asInstanceOf[FloatControl]
	        	
	        	add(new ComponentUI(audioComp1), 300, 250, true)
	        	add(new ComponentUI(audioComp2), 20, 20, true)
	        	add(new ComponentUI(audioComp3), 300, 20, true)
	        	add(new ComponentUI(audioComp4), 75, 120, true)
	        	
	        	
			}
        )
        
        
    }
    
    class DummyComponent extends Component {
	    override def process(){
	        
	    }
	}
}


