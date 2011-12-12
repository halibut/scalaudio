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
	        	add(new Button("Button"), 50,50)
	        	add(new Button("Button2"), 50,250, true)
	        	add(new Label("Label"), 300,150, true)
	        	add(new IOPortComponent(Color.BLACK, TopSidePort), 30, 400, true)
	        	add(new IOPortComponent(Color.RED, LeftSidePort), 400, 350, true)
	        	add(new IOPortComponent(Color.GREEN, RightSidePort), 150, 150, true)
	        	
	        	val audioComp1 = new DummyComponent() with ComponentInputs with ComponentOutputs with ComponentControls{
	        	    addInput(new Signal(1))
	        	    addInput(new Signal(1))
	        	    addInput(new Signal(1))
	        	    addOutput(new OutputSignal(1))
	        	    addOutput(new OutputSignal(1))
	        	    addOutput(new OutputSignal(1))
	        	    addControl(new FloatControl())
	        	}
	        	
	        	val audioComp2 = new DummyComponent() with ComponentInputs with ComponentOutputs{
	        	    addInput(new Signal(1))
	        	    addInput(new Signal(1))
	        	    addInput(new Signal(1))
	        	    addOutput(new OutputSignal(1))
	        	}
	        	
	        	val audioComp3 = new DummyComponent() with ComponentInputs with ComponentControls{
	        	    addInput(new Signal(1))
	        	    addInput(new Signal(1))
	        	    addControl(new FloatControl())
	        	    addControl(new FloatControl())
	        	    addControl(new FloatControl())
	        	}
	        	
	        	val audioComp4 = new SawWaveGenerator(44100f)
	        	
	        	add(new ComponentUI(audioComp1), 300, 250, true)
	        	add(new ComponentUI(audioComp2), 20, 20, true)
	        	add(new ComponentUI(audioComp3), 300, 20, true)
	        	add(new ComponentUI(audioComp4), 75, 120, true)
			}
        )
        
        
    }
}

class DummyComponent extends Component {
    override def process(){
        
    }
}
