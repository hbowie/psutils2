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

package com.powersurgepub.psutils2.textio;

  import com.powersurgepub.psutils2.ui.*;

  import java.io.*;
  import java.net.*;

/**
  Writes text lines to a local file. Implements TextLineWriter.
 */
public class FileMaker 
    implements 
        TextLineWriter {
  
  private     Trouble               trouble = Trouble.getShared();
  
  private     String                fileEncoding = "UTF-8";
  
  private     boolean               openForOutput = false;
  private     boolean               outOK = true;
  private     File                  outFile;
  private     BufferedWriter        outBuffered;        
  
  /** Creates a new instance of FileMaker */
  public FileMaker (URL url) {
    if (url.getProtocol().equals ("file")) {
      String outFileStr = url.getFile();
      try {
        outFileStr = java.net.URLDecoder.decode(url.getFile(), "UTF-8");
      } catch (IllegalArgumentException e) {
        
      }
      catch (UnsupportedEncodingException uee) {
      
      }
      outFile = new File (outFileStr);
    } else {
      outOK = false;
    }
  }
  
  /** Creates a new instance of FileMaker */
  public FileMaker (String outFileName) {
    outFile = new File (outFileName);
  }
  
  /** Creates a new instance of FileMaker */
  public FileMaker (File outFile) {
    this.outFile = outFile;
  }
  
  public String getFileName () {
    return outFile.toString();
  }
  
  public boolean exists () {
    return outFile.exists();
  }
  
  public boolean canWrite () {
    return outFile.canWrite();
  }
  
  public boolean isFile () {
    return outFile.isFile();
  }
  
  public String toString () {
    return outFile.toString();
  }
  
  public boolean openForOutput () {
    outOK = true;
    openForOutput = true;
    
    File parent = outFile.getParentFile();
    if (parent != null) {
      if (! parent.exists()) {
        boolean ok = parent.mkdirs();
      }
    }
    try {
      FileOutputStream outStream = new FileOutputStream (outFile);
      OutputStreamWriter outWriter = new OutputStreamWriter (outStream, fileEncoding);
      outBuffered = new BufferedWriter (outWriter);
    } catch (IOException e) {
      outOK = false;
      openForOutput = false;
      trouble.report 
          ("File "+ outFile.toString() + " could not be opened for output", 
              "File Save Error");  
    }
    return outOK;
  }
  
  public boolean writeLine (String s) {
    boolean ok = write (s);
    if (ok) {
      ok = newLine();
    }
    return ok;
  }
  
  public boolean write (String s) {
    if (! openForOutput) {
      openForOutput();
    }
    if (outOK) {
      try {
        outBuffered.write (s);
      } catch (java.io.IOException e) {
        outOK = false;
        trouble.report 
            ("Trouble writing to file "+ outBuffered.toString(), 
                "File Save Error");  
      }
    } // end if ok so far
    return outOK;
  } // end method write
  
  public boolean newLine () {
    if (outOK) {
      try {
        outBuffered.newLine();
      } catch (java.io.IOException e) {
        outOK = false;
        trouble.report 
            ("Trouble writing to file "+ outBuffered.toString(), 
                "File Save Error");  
      }
    } // end if ok so far
    return outOK;
  } // end method newLine
  
  public boolean flush () {
    if (outOK) {
      try {
        outBuffered.flush();
      } catch (java.io.IOException e) {
        outOK = false;
        trouble.report 
            ("Trouble writing to file " + outBuffered.toString(),
                "File Save Error");
      } // end caught exception
    } // end if ok so far
    return outOK;
  } // end method flush
  
  public boolean close () {
    if (outOK) {
      try {
        outBuffered.close();
      } catch (java.io.IOException e) {
        outOK = false;
        trouble.report 
            ("Trouble writing to file " + outBuffered.toString(),
                "File Save Error");
      } // end caught exception
    } // end if ok so far
    
    return outOK;
  }
  
  public boolean isOK () {
    return outOK;
  }
  
  public File getFile () {
    return outFile;
  }
  
  public String getDestination () {
    return outFile.toString();
  }
  
}
