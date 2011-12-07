/*
 * Copyright (C) 2011 instaBetter Software <http://insta-better.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.instabetter.scalaudio
package components
package io

import javax.sound.sampled.DataLine
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.SourceDataLine
import javax.sound.sampled.Mixer
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.TargetDataLine
import javax.sound.sampled.AudioFormat.Encoding

object AudioIO{
    
    val posRangeForBytes:Array[Int] = Array(0, 
            0x0000007F, 0x00007FFF, 0x007FFFFF, 0x7FFFFFFF)
    
    val negRangeForBytes:Array[Int] = Array(0, 
            0xFFFFFF80, 0xFFFF8000, 0xFF800000, 0x80000000)
            
    def createAudioFormat(sp:SignalProperties, channels:Int, bigEndian:Boolean):AudioFormat = {
        new AudioFormat(Encoding.PCM_SIGNED,
                sp.sampleRate, 
	            sp.bytesPerChannel * 8, 
	            channels, 
	            sp.bytesPerChannel * channels,
	            sp.sampleRate,
	            bigEndian)
    }
    
    def createAudioFormat(sampleRate:Float, bytesPerChannel:Int, channels:Int, bigEndian:Boolean):AudioFormat = {
        new AudioFormat(Encoding.PCM_SIGNED,
                sampleRate, 
	            bytesPerChannel * 8, 
	            channels, 
	            bytesPerChannel * channels,
	            sampleRate,
	            bigEndian)
    }
    
    def createOutputLineInfo(af:AudioFormat, bufferSizeInSamples:Int):DataLine.Info = {
        val frameSize = af.getFrameSize();
        new DataLine.Info(classOf[SourceDataLine], af, bufferSizeInSamples * frameSize)
	}
    
    def createInputLineInfo(af:AudioFormat, bufferSizeInSamples:Int):DataLine.Info = {
        val frameSize = af.getFrameSize();
        new DataLine.Info(classOf[TargetDataLine], af, bufferSizeInSamples * af.getFrameSize())
	}
    
	def getCompatibleIODevices(lineInfo:javax.sound.sampled.DataLine.Info):Seq[Mixer] = {
	    val mixers = AudioSystem.getMixerInfo()	.map{AudioSystem.getMixer(_)}
	    
	    mixers.filter{mixer =>
	        mixer.open()
    	    val supported = mixer.isLineSupported(lineInfo);
	        mixer.close()
    	    supported
    	}
	}
	
	/**
	 * Copy the value of the sample into the specified byte array. Note: the byteArray must be long
	 * enough to hold the entire sample (arrayOffset + sample.size * bytesPerChannel)
	 * @param sample The audio sample to convert and copy to the array (each Float in the array
	 * corresponds to the value of each channel) 
	 * @param byteArray The array to store the sample in
	 * @param byteArrayOffset The offset index of the byte array to start copying the bytes into
	 * @param bytesPerChannel the number of bytes in each channel
	 */
	def copySignalSampleToByteArray(sample:Array[Float], byteArray:Array[Byte], byteArrayOffset:Int, bytesPerChannel:Int, bigEndian:Boolean) {
	    var channel = 0
	    while(channel < sample.size){
	        val channelArrayOffset = byteArrayOffset + channel * bytesPerChannel
	        val channelVal = sample(channel)
	        
	        copyChannelSampleToByteArray(channelVal, byteArray, channelArrayOffset, bytesPerChannel, bigEndian)
	        channel += 1
	    }
	}
	
	/**
	 * Convert the byte array to an array of Float values that represent the channels in
	 * the audio signal. Note: the byteArray must be long enough to hold the entire sample 
	 * (arrayOffset + sample.size * bytesPerChannel)
	 * @param bytes The array containing the sample
	 * @param byteArrayOffset The offset index of the byte array to start copying the bytes from
	 * @param sample The audio sample Float array that will store the converted data
	 * @param bytesPerChannel the number of bytes in each channel
	 */
	def copyByteArrayToSignalSample(bytes:Array[Byte], byteArrayOffset:Int, sample:Array[Float], bytesPerChannel:Int, bigEndian:Boolean) {
	    var channel = 0
	    while(channel < sample.size){
	        val channelArrayOffset = byteArrayOffset + channel * bytesPerChannel
	        
	        val channelVal = converByteArrayToChannelSample(bytes, channelArrayOffset, bytesPerChannel, bigEndian)
	        sample(channel) = channelVal
	        
	        channel += 1
	    }
	}
	
	private def copyChannelSampleToByteArray(channelSample:Float, byteArray:Array[Byte], arrayOffset:Int, length:Int, bigEndian:Boolean) {
	    val intVal = if(channelSample >= 0){
	        (channelSample * posRangeForBytes(length)).asInstanceOf[Int]
	    }
	    else{
	        (-channelSample * negRangeForBytes(length)).asInstanceOf[Int]
	    }
	    
	    var byteMask = 0x000000FF
	    var mostSigByte = 0
	    var curByte = length - 1
	    var offsetDir = -1
	    if(!bigEndian){
	        mostSigByte = length - 1
	        curByte = 0
	        offsetDir = 1
	    }
	    
	    var loop = 0
	    while(curByte != (mostSigByte+offsetDir)){
	        var byteVal = ((intVal & byteMask) >> (8*loop))
	        byteArray(arrayOffset + curByte) = byteVal.asInstanceOf[Byte]
	        
	        byteMask <<= 8
	        curByte += offsetDir
	        loop += 1
	    }
	    
	    val some = 1
	}
	
	private def converByteArrayToChannelSample(byteArray:Array[Byte], arrayOffset:Int, bytesPerChannel:Int, bigEndian:Boolean):Float = {
	    
	    var mostSigByte = 0
	    var byteOffset = (bytesPerChannel-1)
	    var offsetDir = -1
	    if(!bigEndian){
	        mostSigByte = (bytesPerChannel-1)
	        byteOffset = 0
	        offsetDir = 1
	    }
	    
	    var intVal = 0
	    var byte = 0
	    while(byte < bytesPerChannel){
	        var byteVal = byteArray(arrayOffset + byteOffset).asInstanceOf[Int]
		    if(byteOffset != mostSigByte && byteVal < 0){
	            byteVal = -(byteVal ^ 0x000000FF) - 1 
	        }
	        val shiftedVal = byteVal << (8*byte)
	        intVal = intVal + shiftedVal
	        
	        byteOffset += offsetDir
	        byte += 1
	    }
	    
	    if(intVal >= 0){
	    	intVal.asInstanceOf[Float] / posRangeForBytes(bytesPerChannel)   
	    }
	    else{
	    	-intVal.asInstanceOf[Float] / negRangeForBytes(bytesPerChannel)
	    }
	}
	
}

