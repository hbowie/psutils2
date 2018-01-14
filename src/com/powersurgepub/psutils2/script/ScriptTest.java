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

package com.powersurgepub.psutils2.script;


/**
   Method to test the other classes in the package.
  
   @author Herb Bowie (<a href="mailto:herb@powersurgepub.com">
           herb@powersurgepub.com</a>)<br>
           of PowerSurge Publishing (<A href="http://www.powersurgepub.com">
           www.powersurgepub.com</a>)
  
 */

public class ScriptTest {  
  
  /** 
     Tests some of the script classes.
   */
  public static void main (String args[]) {
    
    testRemovePath ("/Users/hbowie/test/test.doc", "/Users/hbowie");
    testRemovePath ("/Users/hbowie/test2.doc", "/Users/hbowie/test");
    testRemovePath ("/Users/hbowie/test2.doc", "/Users/hbowie/test/tdfczar");
    testRemovePath ("c:Users/hbowie/test2.doc", "c:Users/hbowie/test/tdfczar");
    
    System.out.flush();
  } // end of main method
  
  private static void testRemovePath (String f, String p) {
    System.out.println("");
    ScriptAction test = new ScriptAction ("Input", "Open", "", "", f);
    System.out.println ("Original file name: " + f);
    System.out.println ("Path:               " + p);
    test.removePath(p, "");
    System.out.println ("Modified file name: " + test.getValue());
    test.restorePath(p, "");
    System.out.println ("Restored file name: " + test.getValue());
  }
  
} // end of class ScriptTest