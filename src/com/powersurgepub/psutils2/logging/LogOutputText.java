/*
 * Copyright 1999 - 2017 Herb Bowie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.powersurgepub.psutils2.logging;

  import com.powersurgepub.psutils2.basic.*;

  import java.io.*;

  import javafx.scene.control.*;
             
/**

   A log destination that writes log records 
   to a JTextArea.
 */

public class LogOutputText extends LogOutput {

  private    StringBuilder  logText = new StringBuilder();
  private    TextArea			  logTextArea = null;
  private    BufferedWriter logFileBufWriter;
  private    int            logFileLineNumber;
  private    String         logFileLine;
  private    int            logFileLineLength;

  /**
     The "noarg" constructor. The text area will have to be passed separately. 
   */
  public LogOutputText () {

  } 

/**
   The constructor with a passed text area.

   @param textArea the text area to be used for output. 
 */
  public LogOutputText (TextArea textArea) {
    logTextArea = textArea;
  } 

/**
   Set the text area to be used for output.

   @param textArea the text area to be used for output. 
 */
  public void setTextArea (TextArea textArea) {
    logTextArea = textArea;
  } 

  /**
     Writes a line of output to the log text area.

     @param line  line of data to be written to the log file.
   */  
  public void writeLine (String line) {
    logText.append (line + GlobalConstants.LINE_FEED_STRING);
    if (logTextArea != null) {
      logTextArea.setText(logText.toString());
    }
  } 

  public void open () {
    super.open();
  } 
  
  public void close () {
    super.close();
  } 

  public String toString () {
    return "LogOutputText";
  } 

} 
