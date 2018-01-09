/*
 * Copyright 1999 - 2014 Herb Bowie
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

  import com.powersurgepub.psutils2.strings.*;
  import com.powersurgepub.psutils2.ui.*;

  import java.io.*;
  import java.net.*;

/**
 Reads lines from a file. Implements TextLineReader.
 */
public class FileLineReader
    implements
        TextLineReader {
  
  private     String                fileEncoding = "UTF-8";
  private     Trouble               trouble = Trouble.getShared();
  private     File                  inFile = null;
  private     URL                   inURL = null;
  private     InputStreamReader     inReader;
  private     BufferedReader        inBuffered;
  private     String                line = "";
  private     int                   lineNumber = 0;
  private     boolean               inOK = true;
  private     boolean               openForInput = false;
  private     boolean               atEnd = false;
  
  /** Creates a new instance of FileLineReader */
  public FileLineReader (URL url) {
    inURL = url;
    if (url.getProtocol().equals ("file")) {
      inFile = new File (StringUtils.restoreSpacesToURL(url.getFile()));
    } 
    // System.out.println("FileLineReader constructor with URL");
    // System.out.println("  URL: " + inURL.toString());
    // System.out.println("  File: " + inFile.toString());
  }
  
  /** Creates a new instance of FileLineReader */
  public FileLineReader (String inName) {
    try {
      inURL = new URL(inName);
    } catch (MalformedURLException e) {
      inURL = null;
      inFile = new File (inName);
    }
    
  }
  
  public FileLineReader (String path, String fileName) {
    inFile = new File (path, fileName);
  }
  
  /** Creates a new instance of FileLineReader */
  public FileLineReader (File file) {
    this.inFile = file;
  }
  
  public boolean exists () {
    return (inFile != null && inFile.exists());
  }
  
  public boolean isFileObject () {
    return (inFile != null);
  }
  
  public boolean isFile () {
    return (inFile != null && inFile.isFile());
  }
  
  public boolean canRead () {
    return (inFile != null && inFile.canRead());
  }
  
  /**
   Any exceptions so far?
  
   @return True if no exceptions; false otherwise. 
  */
  public boolean isOK () {
    return inOK;
  }
  
  public File getFile () {
    return inFile;
  }
  
  public URL getURL () {
    return toURL();
  }
  
  public URL toURL () {
    if (inURL != null) {
      return inURL;
    } 
    else
    if (inFile != null) {
      try {
        return inFile.toURI().toURL();
      } catch (MalformedURLException e) {
        return null;
      }
    } else {
      return null;
    }
  }
  
  /**
     Indicates whether the file has reached its end.</p>
    
     @return    True if file is at end, false if there are more records to read.
   */
  public boolean isAtEnd() {
    return atEnd;
  }
  
  /**
  Open for input.
  
  @return True if everything opened OK. 
  */
  public boolean open () {
    inOK = true;
    openForInput = true;
    atEnd = false;
    
    if (inFile != null) {
      inOK = openFile();
    }
    else
    if (inURL != null) {
      inOK = openURL();
    } else {
      inOK = false;
      trouble.report 
          ("No file specified", 
              "File Open Error");  
    }
    
    if (! inOK) {
      openForInput = false;
      atEnd = true;
    }
    return inOK;
  }
  
  private boolean openFile() {
    boolean ok = true;
    if (! exists() ) {
      ok = false;
      trouble.report 
          ("File "+ toString() + " could not be found", 
              "File Open Error");   
    }
    else
    if (! isFile () ) {
      ok = false;
      trouble.report 
          ("File "+ toString() + " is not a file", 
              "File Open Error");  
    }
    else
    if (! this.canRead () ) {
      ok = false;
      trouble.report 
          ("File "+ toString() + " cannot be read", 
              "File Open Error"); 
    } else {
      try {
        FileInputStream fileInputStream = new FileInputStream(inFile);
        inReader = new InputStreamReader (fileInputStream, fileEncoding);
        inBuffered = new BufferedReader (inReader);
      } catch (IOException e) {
        ok = false;
        trouble.report 
            ("File "+ toString() + " could not be opened for input", 
                "File Open Error");  
      }
    }
    if (! ok) {
      inOK = ok;
    }
    return ok;
  }
  
  private boolean openURL() {
    boolean ok = true;
    try {
	    inBuffered = new BufferedReader(
				new InputStreamReader(
				inURL.openStream()));
    } catch (IOException e) {
      ok = false;
      trouble.report 
          ("URL "+ inURL.toString() + " could not be opened for input", 
              "URL Open Error");  
    }
    if (! ok) {
      inOK = ok;
    }
    return ok;
  }
  
  /**
     Returns the next line from the text file. <p>
     
     If the text file has not yet been opened, then the first execution of this 
     method will automatically attempt to open the file. When the end of the file 
     is encountered, an empty String will be returned as the next line, the 
     atEnd variable will be turned on, and the file will be closed. 
     
     @return    The next line in the file (or an empty string at end of file).
   */
  public String readLine () {
    line = "";
    if ((! openForInput) && (! atEnd)) {
      open();
    }
    if (openForInput && inOK) {
      try { 
        line = inBuffered.readLine();
        if (line == null) {
          line = "";
          atEnd = true;
        } else {
          lineNumber++;
        }
      } catch (IOException e) {
        line = "";
        atEnd = true;
        inOK = false;
        trouble.report 
            ("File "+ inFile.toString() + " could not be read", 
                "File Read Error"); 
      }
      if (atEnd && openForInput) {
        close();
      }
    }
    return line;
  }
  
  /**
     Returns the number of the last line read from or written to the file.
     
     @return  The line number of the last line read or written.
   */
  public int getLineNumber() {
    return lineNumber;
  } 
  
  /**
   Close the input file. 
  
   @return True if no exceptions. 
  */
  public boolean close () {
    
    if (openForInput) {
      try {
        inBuffered.close ();
      } catch (IOException e) {
        inOK = false;
      }
      openForInput = false;
      atEnd = true;
    }

    return inOK;
  }
  
  /**
   Return a string representation of the name/location of the 
   data source being read. 
  
   @return A string identifying the data source.
  */
  public String toString () {
    if (inFile != null) {
      return inFile.toString();
    } else {
      return inURL.toString();
    }
  }

}
