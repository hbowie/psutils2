/*
 * Copyright 2014 - 2014 Herb Bowie
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

package com.powersurgepub.psutils2.template;

/**
 A single line, with a boolean to indicate whether there is to be a line
 break afterwards. 

 @author Herb Bowie
 */
public class LineWithBreak {
  
  private String  line = null;
  private boolean lineBreak = true;
  
  public LineWithBreak() {
    line = "";
    lineBreak = true;
  }
  
  public LineWithBreak (String line) {
    this.line = line;
    lineBreak = true;
  }
  
  public LineWithBreak (String line, boolean lineBreak) {
    this.line = line;
    this.lineBreak = lineBreak;
  }
  
  public LineWithBreak (StringBuilder line) {
    this.line = line.toString();
    lineBreak = true;
  }
  
  public LineWithBreak (StringBuilder line, boolean lineBreak) {
    this.line = line.toString();
    this.lineBreak = lineBreak;
  }
  
  public void setLine (String line) {
    this.line = line;
  }
  
  public void setLine (StringBuilder line) {
    this.line = line.toString();
  }
  
  public String getLine() {
    return line;
  }
  
  public void setLineBreak(boolean lineBreak) {
    this.lineBreak = lineBreak;
  }
  
  public boolean getLineBreak() {
    return lineBreak;
  }

}
