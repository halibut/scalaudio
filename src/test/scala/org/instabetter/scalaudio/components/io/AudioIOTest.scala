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

import org.junit.Assert._
import org.junit.Test

class AudioIOTest {

    @Test
    def testConvertFloatToByteArrayAndBack{
        val bytes:Array[Byte] = new Array(4)
        val floats:Array[Float] = new Array(1)
        
        val testValues:Seq[Float] = Seq(-1.0f, -0.5f, 0.0f, 0.5f, 1.0f)
        val sampleSizeAndPrecisionValues:Seq[(Int,Float)] = Seq((1,0.01f),(2,0.0001f),(3,0.000001f),(4,0.00000001f))
        val endianValues:Seq[Boolean] = Seq(true, false)
        
        for(bigEndian <- endianValues;
        	sampleSizeAndPrecis <- sampleSizeAndPrecisionValues;
            testVal <- testValues){
            
            val (sampleSize, precision) = sampleSizeAndPrecis
            
        	floats(0) = testVal
	        AudioIO.copySignalSampleToByteArray(floats, bytes, 0, sampleSize, bigEndian);
            AudioIO.copyByteArrayToSignalSample(bytes, 0, floats, sampleSize, bigEndian);
	        
	        assertEquals("Bytes Per Sample: "+sampleSize+",  Big Endian: "+bigEndian+"  -  ",
	                testVal, floats(0), precision)
        }
    }
}