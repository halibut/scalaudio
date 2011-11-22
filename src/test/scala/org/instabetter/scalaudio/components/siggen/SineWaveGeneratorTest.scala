package org.instabetter.scalaudio
package components
package siggen

import org.junit.Test
import org.junit.Assert._

class SineWaveGeneratorTest {

    @Test
    def testSignalGen(){
        implicit val sp = new SignalProperties(10000f)
        
        //create a Sine wave with zero offset and 100 hz frequency
        val generator = new SineWaveGenerator(0f)
        val channelOut = new Channel()
        val signalOut = new Signal(Vector(channelOut))
        
        generator.outputs.setLine(signalOut)
        generator.setFrequency(100f)
        
        for(i <- 0 until 10000){
            generator.step()
            val freqAdj = 100f / 10000f
            val cycles = i * freqAdj
            val sineInput = (2.0f * math.Pi).asInstanceOf[Float] * cycles
            val expected = math.sin(sineInput).asInstanceOf[Float]

            assertEquals("Failed for step "+i,expected, channelOut.read(), 0.00001f)
        }        
        
    }
    
    @Test
    def testSignalGenWithOffset(){
        implicit val sp = new SignalProperties(10000f)
        
        //create a Sine wave with 1.5 second offset and 100 hz frequency
        val generator = new SineWaveGenerator(1.5f)
        val channelOut = new Channel()
        val signalOut = new Signal(Vector(channelOut))
        
        generator.outputs.setLine(signalOut)
        generator.setFrequency(100f)
        
        for(i <- 0 until 10000){
            generator.step()
            val freqAdj = 100f / 10000f
            val cycles = (i+15000) * freqAdj
            val sineInput = (2.0f * math.Pi).asInstanceOf[Float] * cycles
            val expected = math.sin(sineInput).asInstanceOf[Float]

            assertEquals("Failed for step "+i,expected, channelOut.read(), 0.00001f)
        }        
        
    }
}