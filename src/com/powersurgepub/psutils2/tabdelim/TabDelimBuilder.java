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

package com.powersurgepub.psutils2.tabdelim;

  import com.powersurgepub.psutils2.basic.*;

/**
   A tab-delimited record built from a series of strings. 
  
   @author Herb Bowie (<a href="mailto:herb@powersurgepub.com">
           herb@powersurgepub.com</a>)<br>
           of PowerSurge Publishing (<A href="http://www.powersurgepub.com/software/">
           www.powersurgepub.com/software</a>)
 */
public class TabDelimBuilder {

  /** Number of tokens so far included in tab-delimited record. */
  private  int              tokenCount;
  
  /** Buffer used to build the tab-delimited record. */
  private  StringBuffer     record;
  
  /**
     Constructs a new builder.
   */
  public TabDelimBuilder () {
    record = new StringBuffer();
    tokenCount = 0;
  }
  
  /**
     Adds another token to the tab-delimited record being built.
    
     @parm inToken Next string to be added, with a tab between
                   adjacent tokens.
   */
  public void nextToken (String inToken) {
    if (tokenCount == 0) {
      record.append (inToken);
    } else {
      record.append (GlobalConstants.TAB_STRING + inToken);
    }
    tokenCount++;
  }

  /**
     Returns the finished tab-delimited record.
    
     @return Tab-delimited record containing all tokens passed so far.
   */
  public String toString () {
    return record.toString();
  }
}