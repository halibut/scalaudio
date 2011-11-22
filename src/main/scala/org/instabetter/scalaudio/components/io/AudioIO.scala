package org.instabetter.scalaudio
package components
package io

import javax.sound.sampled.DataLine
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.SourceDataLine
import javax.sound.sampled.Mixer
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.TargetDataLine

object AudioIO{

    def createAudioFormat(sp:SignalProperties, channels:Int):AudioFormat = {
        new AudioFormat(sp.sampleRate, 
	            sp.bytesPerChannel * 8, 
	            channels, 
	            true, 
	            true)
    }
    
    def createOutputLineInfo(sp:SignalProperties, af:AudioFormat):DataLine.Info = {
        val frameSize = af.getFrameSize();
        val maxBufferSize = (1 + sp.maxDelaySamples * 10) * af.getFrameSize()
	    new DataLine.Info(classOf[SourceDataLine], af, maxBufferSize)
	}
    
    def createInputLineInfo(sp:SignalProperties, af:AudioFormat):DataLine.Info = {
        val frameSize = af.getFrameSize();
        val maxBufferSize = (1 + sp.maxDelaySamples * 10) * af.getFrameSize()
	    new DataLine.Info(classOf[TargetDataLine], af, maxBufferSize)
	}
    
	def getCompatibleIODevices(lineInfo:javax.sound.sampled.DataLine.Info):Seq[Mixer] = {
	    AudioSystem.getMixerInfo()
	    	.map{AudioSystem.getMixer(_)}
	    	.filter{mixer =>
	    	    val supported = mixer.isLineSupported(lineInfo);
	    	    supported
	    	}
	}
	
	def getSignalAsByteArray(signal:IndexedSeq[Float], bytesPerChannel:Int):Array[Byte] = {
	    val array = new Array[Byte](bytesPerChannel * signal.size)
	    
	    for(channelNum <- 0 until signal.size){
	        val normSignal = math.min(math.max(-1.0f, signal(channelNum)), 1.0f)
	        val intVal = math.round(normSignal * dataTypeRange(bytesPerChannel))
	        
	        val arrayOffset = channelNum * bytesPerChannel 
	        
	        bytesPerChannel match{
	            case 1 => array(arrayOffset) = intVal.asInstanceOf[Byte]
	            case 2 =>
	                array(arrayOffset) = ((intVal & 0x0000FF00) >> 8).asInstanceOf[Byte]
	                array(arrayOffset + 1) = (intVal & 0x000000FF).asInstanceOf[Byte]
	            case 4 =>
	                array(arrayOffset) = ((intVal & 0xFF000000) >> 24).asInstanceOf[Byte]
	                array(arrayOffset + 1) = ((intVal & 0x00FF0000) >> 16).asInstanceOf[Byte]
	                array(arrayOffset + 2) = ((intVal & 0x0000FF00) >> 8).asInstanceOf[Byte]
	                array(arrayOffset + 3) = (intVal & 0x0000FF).asInstanceOf[Byte]
	        }
	    }

	    array
	}
	
	private def dataTypeRange(bytes:Int):Int = {
	    bytes match{
	        case 1 => Byte.MaxValue
	        case 2 => Short.MaxValue
	        case 4 => Int.MaxValue
	        case _ => throw new IllegalArgumentException("Data type is not supported: " + bytes + " bytes.")
	    }
	}
	
}