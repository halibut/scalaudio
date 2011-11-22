package org.instabetter.scalaudio
package components

trait ComponentIOModule[T <: Line] {
	
    def numLines:Int = lines.size
    def lines:IndexedSeq[T]
    def line(lineName:String):Option[T] = {
        lines.find(_.name == lineName)
    }
}

class NoLineIOModule[T <: Line] extends ComponentIOModule[T]{
    override def lines:IndexedSeq[T] = IndexedSeq()
}

class SingleLineIOModule[T <: Line] extends ComponentIOModule[T]{
    
    private var _line:Option[T] = None
        
    override def lines:IndexedSeq[T] = IndexedSeq(_line).flatten
    
    def setLine(line:T){ _line = Option(line) }
    def line:T = _line.get 
}

class MultiLineIOModule[T <: Line] extends ComponentIOModule[T]{
    
    private var _lines:Vector[T] = Vector()
        
    override def lines:IndexedSeq[T] = _lines
    
    def addLine(line:T){ _lines :+= line }
    def removeLine(line:T){ _lines = _lines.filterNot(_ eq line) }
    def removeLine(index:Int){ removeLine(_lines(index)) }
    def removeLine(name:String){ _lines = _lines.filterNot(_.name eq name) }
}