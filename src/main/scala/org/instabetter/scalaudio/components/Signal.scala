package org.instabetter.scalaudio.components

trait Signal extends Connectable[Array[Float]] with ComponentPort{

    def setNumChannels(numChannels:Int)
    
    def channels():Int = { getValue().size }
    
    def write(signalValue:Float){
        var i = 0
        val array = getValue()
        while(i < array.size){
            array(i) = signalValue
            i+=1
        }
    }
    
    def write(channelValues:Array[Float]){
        Array.copy(channelValues, 0, getValue(), 0, getValue().size)
    }
    
    def read():Array[Float] = {
        getValue().clone()
    }
    
    def readAt(index:Int):Float = {
        getValue()(index)
    }
    
    def modify(modifyFunc:(Float)=>Float){
        var i = 0
        val array = getValue()
        while(i < array.size){
            array(i) = modifyFunc(array(i))
            i+=1
        }
    }
}