/*
 * Copyright 2007 - 2013 Herb Bowie
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

package com.powersurgepub.psutils2.strings;

/**
 *
 * @author hbowie
 */
public class CommaList {
  
  private StringBuffer list = new StringBuffer();
  
  /** Creates a new instance of CommaList */
  public CommaList() {
  }
  
  public void append (String next) {
    if (list.length() > 0) {
      list.append (", ");
    }
    list.append (next);
  }
  
  public String toString () {
    return list.toString();
  }
  
}