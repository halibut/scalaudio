package org.instabetter.scalaudio


import components.io.AudioIO
import components.SignalProperties
import components.siggen.SineWaveGenerator
import components.siggen.SawWaveGenerator
import components.siggen.TriangleWaveGenerator
import components.siggen.SquareWaveGenerator
import components._
import components.controls._
import components.convert.SignalDrivenControl
import components.io.AudioOut
import javax.sound.sampled.Mixer




object JSoundTest2 {
    
    def main(args:Array[String]){
        
        implicit val sp = SignalProperties(
                sampleRate = 44100f,
                maxDelaySeconds = .1f)
    	
        val af = AudioIO.createAudioFormat(sp, 1)
        val outputLineInfo = AudioIO.createOutputLineInfo(sp, af)
        
        var targetMixer:Mixer = null
        AudioIO.getCompatibleIODevices(outputLineInfo).foreach{mixer =>
            println(mixer.getMixerInfo() + " - " + mixer)
            if(mixer.getMixerInfo().toString().contains("Realtek High Definition Audio") && targetMixer == null){
                targetMixer = mixer
            }
        }
        
        
        //Define the frequency sweep signal generator
        //Sweeps between 250-350 Hz every 1/5 second
        val freqSweep = new SineWaveGenerator() with SignalOutputControls
        freqSweep.setFrequency(.25f)
        freqSweep.setGain(200.0f)
        freqSweep.setAmplitudeOffset(250.0f)
        
        //Define the audio signal generator
        val signalGen = new SquareWaveGenerator()
        
        //Define the output device
        val audioOut = new AudioOut(1)
        audioOut.setOutputDevice(targetMixer)
        
        //Wire the 3 components together
        freqSweep.signalOutput --> signalGen.frequencyControl
        signalGen.signalOutput --> audioOut.audioSignal
        
        
        val startTime = System.nanoTime()
        var curTime = startTime
        var curSample = 0L
        var printSamples = 0L
        
        var bufferedSamples = 0L
        val minBuffer = sp.maxDelaySamples / 10
        val sleepTime = (1000 * sp.maxDelaySeconds / 10).asInstanceOf[Int] 
        
        var lastSkippedSamples = 0L
        
        var printTime = 0L 
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
                freqSweep.processSignal()
                
                //Generate the audio frequency
                signalGen.processSignal()
                
                //Send the audio signal to the output device
                audioOut.processSignal()
            }
            
            
            printSamples += samplesToWrite
            if(printSamples >= (sp.sampleRate.asInstanceOf[Int]/2)){
                val totalSkippedSamples = audioOut.getSkippedSamples()
                val skippedSamples = totalSkippedSamples - lastSkippedSamples
                lastSkippedSamples = totalSkippedSamples
                
                val avgProcessTime = 0.000001 * printTime / printSamples
                
                println("Samples written: " + printSamples 
                        + "   Samples skipped: " + skippedSamples 
                        + "   Seconds: " + elapsedSeconds
                		+ "   Avg Process Time: " + avgProcessTime +" ms")
                println(signalGen.getFrequency() + " Hz   Samples: " + printSamples + "   Seconds: " + elapsedSeconds)
                printSamples -= (sp.sampleRate.asInstanceOf[Int]/2).asInstanceOf[Long]
                printTime = 0L
            }
            
            val endTime = System.nanoTime()
            val diffTime = endTime - curTime
            printTime += diffTime
            val processTime = (0.000000001 * (diffTime)).asInstanceOf[Int]
            val adjustedSleepTime = sleepTime - processTime
            
            if(adjustedSleepTime > 0){
            	Thread.sleep(adjustedSleepTime)
            }
        }
    }
    
}