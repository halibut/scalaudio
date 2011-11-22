package org.instabetter.scalaudio


import components.io.AudioIO
import components.SignalProperties
import components.siggen.SineWaveGenerator
import components.sigmod.Gain
import components.sigmod.AmpOffset
import components.siggen.SawWaveGenerator
import components.Signal
import components.Channel
import components.convert.SignalDrivenControl
import components.io.AudioOut
import components.Component
import components.SingleLineIOModule
import components.MultiLineIOModule
import javax.sound.sampled.Mixer
import org.instabetter.scalaudio.components.Control
import org.instabetter.scalaudio.components.siggen.SquareWaveGenerator


object JSoundTest2 {
    
    def main(args:Array[String]){
        
        implicit val sp = SignalProperties(
                sampleRate = 44100f,
                maxDelaySeconds = .2f)
    	
        val af = AudioIO.createAudioFormat(sp, 1)
        val outputLineInfo = AudioIO.createOutputLineInfo(sp, af)
        
        var targetMixer:Mixer = null
        AudioIO.getCompatibleIODevices(outputLineInfo).foreach{mixer =>
            println(mixer.getMixerInfo() + " - " + mixer)
            if(targetMixer == null){
                targetMixer = mixer
            }
        }
        
        
        //Define the frequency sweep signal generator
        val freqSweep = new SquareWaveGenerator()
        freqSweep.setFrequency(.1f)
        val freqSweepGain = new Gain()
        freqSweepGain.setGain(100.0f)
        val freqSweepOffset = new AmpOffset
        freqSweepOffset.setOffset(300.0f)
        
        //Wire the components of the sweep signal generator
        connect(freqSweep, freqSweepGain, 1)
        connect(freqSweepGain, freqSweepOffset, 1)
                
        
        //Define the audio signal generator
        //and wire the frequency sweep to its frequency control
        val signalGen = new SquareWaveGenerator()
        connectToControl(freqSweepOffset, signalGen)
        
        //Define the output device and wire the signal
        //generator to it
        val audioOut = new AudioOut()
        connect(signalGen, audioOut, 1)
        audioOut.setOutputDevice(targetMixer)
        
        val startTime = System.nanoTime()
        var curTime = startTime
        var curSample = 0L
        var printSamples = 0L
        
        var bufferedSamples = 0L
        val minBuffer = sp.maxDelaySamples / 10
        val sleepTime = (1000 * sp.maxDelaySeconds / 10).asInstanceOf[Int] 
        
        var lastSkippedSamples = 0L
        while(true){
            val lastTime = curTime
            curTime = System.nanoTime()
            val elapsedNanos = curTime - startTime
            val elapsedSeconds = (0.000000001 * elapsedNanos)
            
            val lastSamples = curSample
            val elapsedSamples = (sp.sampleRate * elapsedSeconds).asInstanceOf[Long]
            
            val samplesSinceWait = elapsedSamples - lastSamples
            curSample = elapsedSamples
            
            val samplesToWrite = samplesSinceWait + math.max(0, minBuffer - bufferedSamples)
            bufferedSamples += samplesToWrite - samplesSinceWait
            for(i <- 0 until samplesToWrite.asInstanceOf[Int]){
                //cascade the frequency sweep generator through its signal
                //modifiers
                freqSweep.step()
                freqSweepGain.step()
                freqSweepOffset.step()
                
                //Generate the audio frequency
                signalGen.step()
                
                //Send the audio signal to the output device
                audioOut.step()
            }
            
            
            printSamples += samplesToWrite
            if(printSamples >= (sp.sampleRate.asInstanceOf[Int]/2)){
                val totalSkippedSamples = audioOut.getSkippedSamples()
                val skippedSamples = totalSkippedSamples - lastSkippedSamples
                lastSkippedSamples = totalSkippedSamples
                
                println("Samples written: " + printSamples + "  Samples skipped: " + skippedSamples + "     Seconds: " + elapsedSeconds)
                println(signalGen.getFrequency() + " Hz   Samples: " + printSamples + "   Seconds: " + elapsedSeconds)
                printSamples -= (sp.sampleRate.asInstanceOf[Int]/2).asInstanceOf[Long]
            }
            
            val processTime = (0.000000001 * (System.nanoTime() - curTime)).asInstanceOf[Int]
            val adjustedSleepTime = sleepTime - processTime
            if(adjustedSleepTime > 0){
            	Thread.sleep(adjustedSleepTime)
            }
        }
    }
    
    def connect(from:Component, to:Component, channels:Int){
        val signal = from.outputs match{
            case outputs:SingleLineIOModule[Signal] =>
                if(outputs.numLines == 0){
                    val line = new Signal(channels)
                    line.name = from.name + " output channel."
                    outputs.setLine(line)
                }
                outputs.line
        }
        
        to.inputs match {
            case inputs:SingleLineIOModule[Signal] =>
                inputs.setLine(signal)
            case inputs:MultiLineIOModule[Signal] =>
                inputs.addLine(signal)
        }
    }
    
    def connectToControl(from:Component, to:Component){
        val control = to.controls match {
            case controls:SingleLineIOModule[Control] =>
                controls.line
        }
        
        from.outputs match{
            case outputs:SingleLineIOModule[Signal] =>
                outputs.setLine(new SignalDrivenControl(control))
        }
    }

}