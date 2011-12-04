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

import scala.collection.Seq
import javax.sound.sampled.SourceDataLine
import javax.sound.sampled.Mixer

class OutputDevice() extends AudioDevice[SourceDataLine] {

    override def getAvailableDrivers():Seq[Mixer] = {
        AudioDevice.getOutputDrivers()
    }

    override protected def internalGetDeviceLines():Seq[SourceDataLine] = {
        AudioDevice.getOutputDevices(_jsMixer.get).getOrElse(Seq())   
    }

    override protected def internalOpenLine(): Unit = {
        val line = _jsDataLine.get
        val audioFormat = _jsAudioFormat.get
        
        line.open(audioFormat, _preferredDeviceBufferSize)
    }

}