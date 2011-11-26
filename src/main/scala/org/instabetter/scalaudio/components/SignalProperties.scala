package org.instabetter.scalaudio.components

case class SignalProperties(
        val sampleRate:Float = 44100f, 
        val bytesPerChannel:Int = 2, 
        val maxDelaySeconds:Float = 0.01f){
    val maxDelaySamples = math.floor(sampleRate * maxDelaySeconds).asInstanceOf[Int]
    val inverseSampleRate:Float = 1.0f / sampleRate
}