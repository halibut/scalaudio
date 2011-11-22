package org.instabetter.scalaudio
package components

class Signal(val channels:IndexedSeq[Channel]) extends Line{
    def this(numChannels:Int){
        this((0 until numChannels).map{(ind) => new Channel()})
    }

    require(channels.size > 0, "A signal must contain 1 or more channels")
    
    val numChannels = channels.size
    
    def write(signalValue:Float){
        channels.foreach{_.write(signalValue)}
    }
    
    def write(channelValues:Seq[Float]){
        channels.zip(channelValues).foreach{
            case (channel, value) => channel.write(value)
        }
    }
    
    def read():IndexedSeq[Float] = {
        channels.map(_.read)
    }
    
}