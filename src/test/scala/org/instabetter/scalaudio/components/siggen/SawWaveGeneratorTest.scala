package org.instabetter.scalaudio
package components
package siggen

import org.junit.Test
import org.junit.Assert._

class SawWaveGeneratorTest {

    @Test
    def testSignalGen(){
        implicit val sp = new SignalProperties(10000f)
        
        //create a Saw wave with zero offset and 100 hz frequency
        val generator = new SawWaveGenerator(0f)
        val channelOut = new Channel()
        val signalOut = new Signal(Vector(channelOut))
        
        generator.signalOutput --> signalOut
        generator.setFrequency(100f)
        
        for(i <- 0 until 10000){
            generator.processSignal()
            val remainder = i % 100
            val expected = -1f + 2f * (remainder / 100f)
            assertEquals("Failed for step "+i,expected, channelOut.read(), 0.00001f)
        }        

    }
    
    @Test
    def testSignalGenWithOffset(){
        implicit val sp = new SignalProperties(10000f)
        
        //create a Saw wave with 1.5 second offset and 100 hz frequency
        val generator = new SawWaveGenerator(1.5f)
        val channelOut = new Channel()
        val signalOut = new Signal(Vector(channelOut))
        
        generator.signalOutput --> signalOut
        generator.setFrequency(100f)
        
        for(i <- 0 until 10000){
            generator.processSignal()
            val remainder = (i+15000) % 100
            val expected = -1f + 2f * (remainder / 100f)
            assertEquals("Failed for step "+i,expected, channelOut.read(), 0.00001f)
        }      
        
    }
}