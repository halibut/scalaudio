package org.instabetter.scalaudio
package components
package siggen

import org.junit.Test
import org.junit.Assert._

class SquareWaveGeneratorTest {

    @Test
    def testSignalGen(){
        implicit val sp = new SignalProperties(10000f)
        
        //create a Square wave with zero offset and 100 hz frequency
        val generator = new SquareWaveGenerator(0f)
        val channelOut = new Channel()
        val signalOut = new Signal(Vector(channelOut))
        
        generator.signalOutput --> signalOut
        generator.setFrequency(100f)
        
        for(i <- 0 until 10000){
            generator.processSignal()
            val remainder = i % 100
            val expected = if(remainder < 50 ) -1f else 1f
            assertEquals("Failed for step "+i,expected, channelOut.read(), 0.001f)
        }

    }
    
    @Test
    def testSignalGenWithOffset(){
        implicit val sp = new SignalProperties(10000f)
        
        //create a Square wave with 1.5 second offset and 100 hz frequency
        val generator = new SquareWaveGenerator(1.5f)
        val channelOut = new Channel()
        val signalOut = new Signal(Vector(channelOut))
        
        generator.signalOutput --> signalOut
        generator.setFrequency(100f)
        
        for(i <- 0 until 10000){
            generator.processSignal()
            val remainder = (i+15000) % 100
            val expected = if(remainder < 50 ) -1f else 1f
            assertEquals("Failed for step "+i,expected, channelOut.read(), 0.001f)
        }      
        
    }
}