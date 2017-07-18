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
package com.powersurgepub.psutils2.mkdown;

  import com.powersurgepub.psutils2.strings.*;

/**
 Represents the current state of a markdown file that is being parsed. 

 @author Herb Bowie
 */
public class MarkdownDoc {
  
  public static final String    AUTHOR          = "Author";
  public static final String    BY              = "By";
  public static final String    CREATOR         = "Creator";
  public static final String    DATE            = "Date";
  public static final String    TITLE           = "Title";
  
  private int         lineNumber = 0;
  private int         fileSize = 0;
  private boolean     frontOfFile;
  private String      lastLine = "";
  private TextBuilder title = new TextBuilder();
  private TextBuilder author = new TextBuilder();
  private String      date = "";
  
  public MarkdownDoc() {
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
  
  public void setFrontOfFile (boolean frontOfFile) {
    this.frontOfFile = frontOfFile;
  }
  
  public boolean isFrontOfFile() {
    return frontOfFile;
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
  
  public void setTitle (String title) {
    this.title = new TextBuilder(title);
  }
  
  public String getTitle() {
    return title.toString();
  }
  
  public void setAuthor (String author) {
    this.author = new TextBuilder(author);
  }
  
  public String getAuthor() {
    return author.toString();
  }
  
  public void setDate (String date) {
    this.date = date;
  }
  
  public String getDate() {
    return date;
  }
  
  /**
   Display internal data for testing purposes. 
  */
  public void display() {
    System.out.println("MarkdownDoc.display");
    System.out.println("line number: " + String.valueOf(lineNumber));
    System.out.println("file size: " + String.valueOf(fileSize));
    System.out.println("front of file? " + String.valueOf(frontOfFile));
    System.out.println("last line: " + lastLine);
    if (title != null && title.length() > 0) {
      System.out.println("title: " + title);
    }
    if (author != null && author.length() > 0) {
      System.out.println("author: " + author);
    }
    if (date != null && date.length() > 0) {
      System.out.println("date: " + date);
    }
    System.out.println(" ");
  }

}
