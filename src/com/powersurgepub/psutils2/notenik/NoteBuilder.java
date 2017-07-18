/*
 * Copyright 2013 - 2017 Herb Bowie
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
package com.powersurgepub.psutils2.notenik;

  import com.powersurgepub.psutils2.values.*;

/**
 Represents the current state of a note file that is being parsed. 

 @author Herb Bowie
 */
public class NoteBuilder {
  
  private NoteParms   noteParms;
  
  private int         lineNumber = 0;
  private int         fileSize = 0;
  private boolean     frontOfFile;
  private String      lastLine = "";
  private DataValueStringBuilder lastStringBuilder = null;
  
  public NoteBuilder(NoteParms noteParms) {
    this.noteParms = noteParms;
    frontOfFile = true;
  }
  
  public void setLineNumber (int lineNumber) {
    this.lineNumber = lineNumber;
  }
  
  public void incrementLineNumber() {
    lineNumber++;
  }
  
  public int getLineNumber() {
    return lineNumber;
  }
  
  public void setFileSize (int fileSize) {
    this.fileSize = fileSize;
  }
  
  public void addToFileSize (int addition) {
    fileSize = fileSize + addition;
  }
  
  public int getFileSize() {
    return fileSize;
  }
  
  /**
   Set a flag indicating whether we are still in the front of the file, 
   prior to the body, processing metadata, or whether we've started the body. 
  
   @param frontOfFile True if still front of file. 
  */
  public void setFrontOfFile (boolean frontOfFile) {
    this.frontOfFile = frontOfFile;
  }
  
  
  /**
   Indicates whether we are still in the front of the file, 
   prior to the body, processing metadata, or whether we've started the body. 
  
   @return True if still front of file. 
  */
  public boolean isFrontOfFile() {
    return frontOfFile;
  }
  
  /**
   Set a flag indicating whether we've started the body, or whether 
   we're still in the front of the file, processing metadata. 
  
   @param bodyStarted True if body has been started. 
  */
  public void setBodyStarted (boolean bodyStarted) {
    frontOfFile = false;
  }
  
  /**
   Indicates whether we've started the body, or whether we're still in the
   front of the file, processing metadata. 
  
   @return True if body has been started. 
  */
  public boolean isBodyStarted() {
    return (! frontOfFile);
  }
  
  public void setLastLine (String line) {
    this.lastLine = line;
  }
  
  public boolean hasLastLine() {
    return (lastLine != null && lastLine.length() > 0);
  }
  
  public String getLastLine() {
    return lastLine;
  }
  
  public void setLastStringBuilder(DataValueStringBuilder lastStringBuilder) {
    this.lastStringBuilder = lastStringBuilder;
  }
  
  public boolean appendingStarted() {
    return (lastStringBuilder != null && lastStringBuilder.length() > 0);
  }
  
  public boolean hasLastStringBuilder() {
    return (lastStringBuilder != null);
  }
  
  public DataValueStringBuilder getLastStringBuilder() {
    return lastStringBuilder;
  }
  
  public void appendLineToLastStringBuilder (String line) {
    lastStringBuilder.appendLine(line);
  }
  
  /**
   Display internal data for testing purposes. 
  */
  public void display() {
    System.out.println("NoteBuilder.display");
    System.out.println("line number: " + String.valueOf(lineNumber));
    System.out.println("file size: " + String.valueOf(fileSize));
    System.out.println("front of file? " + String.valueOf(frontOfFile));
    System.out.println("last line: " + lastLine);
    System.out.println(" ");
  }

}
