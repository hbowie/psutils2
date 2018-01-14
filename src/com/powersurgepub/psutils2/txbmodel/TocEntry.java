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

package com.powersurgepub.psutils2.txbmodel;

  import com.powersurgepub.psutils2.strings.*;

/**
 An entry in a generated table of contents. 

 @author Herb Bowie
 */
public class TocEntry {
  
  private int           level = 0;
  private StringBuilder heading = new StringBuilder();
  private String        id = "";
  
  public TocEntry () {
    
  }
  
  public TocEntry (int level, String heading, String id) {
    this.level = level;
    this.heading.append (heading);
    this.id = id;
  }
  
  public void setLevel(int level) {
    this.level = level;
  }
  
  public int getLevel () {
    return level;
  }
  
  public void setHeading (String heading) {
    this.heading = new StringBuilder(heading);
  }
  
  public void append (String str) {
    heading.append(str);
  }
  
  public String getHeading () {
    return heading.toString();
  }
  
  public void setID (String id) {
    this.id = id;
  }
  
  public boolean hasID() {
    return (id.length() > 0);
  }
  
  public boolean lacksID() {
    return (id.length() == 0);
  }
  
  public void deriveID() {
    // Modified for consistency with Marked app.
    id = StringUtils.makeID(heading.toString());
  }
  
  public String getID () {
    return id;
  }
  
  public String getLink() {
    return ("#" + id);
  }

}
