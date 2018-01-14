/*
 * Copyright 1999 - 2013 Herb Bowie
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

package com.powersurgepub.psutils2.txbio;

  import java.util.*;

/**
  This class defines a format/type for storing a TextTree containing
  TextNodes. 
 */
public class TextIOType {
  
  /** A string that briefly describes the format of the storage. */
  private   String                label;
  
  /** The Input/Output module to be used to load/store text in this format. */
  private   TextIOModule          module;
  
  /** Input services provided? */
  private   boolean               goodForInput = true;
  
  /** Output services provided? */
  private   boolean               goodForOutput = false;
  
  /** File extensions that may be associated with this data format. */
  private   Vector                extensions = new Vector();
  
  public TextIOType (
      String label, 
      TextIOModule module, 
      boolean goodForInput, 
      boolean goodForOutput, 
      String extension) {
    this.label = label;
    this.module = module;
    this.goodForInput = goodForInput;
    this.goodForOutput = goodForOutput;
    if (extension != null && extension.length() > 0) {
      this.extensions.add (extension);
    }
  }
  
  public void addExtension (String extension) {
    if (extension != null && extension.length() > 0) {
      this.extensions.add (extension);
    }
  }
  
  public String getLabel () {
    return label;
  }
  
  public TextIOModule getModule () {
    return module;
  }
  
  public boolean isGoodForInput () {
    return goodForInput;
  }
  
  public boolean isGoodForOutput () {
    return goodForOutput;
  }
  
  public String getPrimaryExtension () {
    if (extensions.size() > 0) {
      return getExtension (0);
    } else {
      return "";
    }
  }
  
  public String getExtension (int i) {
    if (i < 0 || i >= extensions.size()) {
      return "";
    } else {
      return (String)extensions.get (i);
    }
  }
  
  public int getExtensionsSize () {
    return extensions.size();
  }
  
  public String toString () {
    return label;
  }

  public String toLongerString () {
    StringBuffer work = new StringBuffer();
    work.append("input? " + String.valueOf(goodForInput));
    work.append("; ");
    work.append("output? " + String.valueOf(goodForOutput));
    work.append("; ");
    work.append(label);
    work.append("; ");
    work.append("extensions: ");
    for (int i = 0; i < extensions.size(); i++) {
      if (i > 0) {
        work.append(", ");
      }
      work.append(getExtension(i));
    }
    work.append("; ");
    work.append("module: " + module.getClass().getName());
    return work.toString();
  }

  public boolean equals (TextIOType type2) {
    return (label.equals (type2.getLabel()));
  }

}
