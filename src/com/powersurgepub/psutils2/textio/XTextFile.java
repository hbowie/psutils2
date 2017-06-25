/*
 * Copyright 2004 - 2017 Herb Bowie
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

  import java.io.*;
  import java.net.*;

/**
   A text file that can be opened for input or output, read from
   or written to, and closed, that will work respectably on any Java
   platform. Use of this class offers two special features. First, it will
   automatically set the file type and creator on a Mac to values typical of
   a standard text file, making an output file easier to open on a Mac. This 
   class will also respect a specified user preference for a particular style
   of line ending to be used on an output file. <p>
 
   Following is typical code that would be used to access XTextFile. <p>
 
  <pre><code>    
    XTextFile in = new XTextFile ("input.txt"); 
    XTextFile out = new XTextFile ("output.txt"); 
    in.openForInput(); 
    out.openForOutput(); 
    String line = in.readLine(); 
    while (! in.isAtEnd()) { 
      out.writeLine(line); 
      line = in.readLine(); 
    } 
    in.close(); 
  </code></pre>
   
   @author Herb Bowie of PowerSurge Publishing
  
 */
public class XTextFile 
    extends File {
  
  /**
   Key for obtaining system properties indicating preferred 
   line ending characters.
   */
  public  final static String   LINE_SEP                = "line.separator";
  
  /** The string used as a user preference key for preferred line endings. */
  public  final static String   LINE_SEP_KEY            = "linesep";
  
  /** 
   The string used to express a preference for traditional mac line endings
   (CR).
   */
  public  final static String   LINE_SEP_PLATFORM_MAC   = "mac";
  
  /** 
   The string used to express a preference for Unix (including Mac OS X) 
   line endings (LF).
   */
  public  final static String   LINE_SEP_PLATFORM_UNIX  = "unix";
  
  /** 
   The string used to express a preference for DOS/PC/Windows  
   line endings (CR/LF).
   */
  public  final static String   LINE_SEP_PLATFORM_DOS   = "dos";
  
  /**
   The preferred line ending characters (CR) typical on a traditional
   Macintosh system.
   */
  public  final static String   LINE_SEP_STRING_MAC     = "\r";
  
  /**
   The preferred line ending characters (LF) typical on a Unix system
   (including Mac OS X).
   */
  public  final static String   LINE_SEP_STRING_UNIX    = "\n";
  
  /**
   The preferred line ending characters (CR/LF) typical on a PC/DOS/Windows
   system.
   */
  public  final static String   LINE_SEP_STRING_DOS     = "\r\n";
  
  /** The name of this file, without any path info. */
  private  String       fileName;
  
  /** The file name including path information. */
  private  String       filePathAndName;
  
  /** Indicates whether URL was passed to constructor. */
  private  boolean       isURL = false;
  
  /** URL that was passed to constructor. */
  private  URL           url;
  
  /** Input stream created from url. */
  private  InputStream   urlIn;
  
  /** Input stream reader for url. */
  private  InputStreamReader urlReader;
  
  /** The reader used when input is requested. */
  private  FileReader   textFileReader;
  
  /** The buffered reader used for input. */
  private  BufferedReader textFileBufReader;
  
  /** The writer used when output is requested. */
  private  FileWriter   textFileWriter;
  
  /** The buffered writer used for output. */
  private  BufferedWriter textFileBufWriter;
  
  /** 
     The sequentially assigned line number of the last record
     read or written.
   */
  private  int          lineNumber;
  
  /** Have we hit end of file on input? */
  protected  boolean    atEnd;
  
  /** The line being read or written. */
  private  String       line;
  
  /** Is this file currently open for input? */
  private  boolean      openAsInput = false;
  
  /** Is this file currently open for output? */
  private  boolean      openAsOutput = false;
  
  /**
     A constructor that accepts a path and file name.
    
     @param path      A path to the directory containing the file.
    
     @param fileName  The file name itself (without path info).
   */
  public XTextFile (String path, String fileName) {
    super (path, fileName);
    this.fileName = fileName;
    filePathAndName = path + File.separatorChar + fileName;
    isURL = false;
    initialize();
  }
  
  /**
     A constructor that accepts a file name.
    
     @param inFileName  A file name.
   */
  public XTextFile (String inFileName) {
    super (inFileName);
    fileName = this.getName();
    filePathAndName = this.getAbsolutePath();
    isURL = false;
    initialize();
  }
  
  /**
     A constructor that accepts two paramenters: a File object
     representing the path to the file, and a String containing the file
     name itself.
     
     @param inPathFile  A path to the directory containing the file
     
     @param inFileName  The file name itself (without path info).
   */
  public XTextFile (File inPathFile, String inFileName) {
    super (inPathFile, inFileName);
    fileName = this.getName();
    filePathAndName = this.getAbsolutePath();
    isURL = false;
    initialize();
  }
  
  /**
     A constructor that accepts a single File object
     representing the file.</p>
     
     @param inFile  The text file itself.
   */  
  public XTextFile (File inFile) {
    super (inFile.getPath());
    fileName = this.getName();
    filePathAndName = this.getAbsolutePath();
    isURL = false;
    initialize();
  }
  
  /**
     A constructor that accepts a URL pointing to a text file.</p>
     
     @param url  The URL of a text file.
   */  
  public XTextFile (URL url) {
    super (url.getFile());
    this.url = url;
    fileName = url.getFile();
    filePathAndName = url.getFile();
    isURL = true;
    initialize();
  }
  
  /**
     Initialize common fields for all constructors.
   */ 
  private void initialize () {
    lineNumber = 0;
    atEnd = false;
  }

  /**
     Returns the next line from the text file. <p>
     
     If the text file has not yet been opened, then the first execution of this 
     method will automatically attempt to open the file. When the end of the file 
     is encountered, an empty String will be returned as the next line, the 
     atEnd variable will be turned on, and the file will be closed. 
     
     @return    The next line in the file (or an empty string at end of file).
    
     @throws IOException            If read failure.
     @throws FileNotFoundException  On first read for file, if file name 
                                    passed to constructor cannot be found.
   */
  public String readLine () 
      throws IOException, FileNotFoundException {
    line = "";
    if ((! openAsInput) && (! atEnd)) {
      this.openForInput();
    }
    if (openAsInput) {
      try { 
        line = textFileBufReader.readLine();
        if (line == null) {
          line = "";
          atEnd = true;
          // close();
        } else {
          lineNumber++;
        }
      } catch (IOException e) {
        line = "";
        atEnd = true;
        throw e;
      }
      if (atEnd && openAsInput) {
        close();
      }
    }
    return line;
  }
  
  /**
     Opens the text file for input. Note that this method need not 
     be explictly executed, since the first execution of readLine 
     will cause this method to be invoked automatically. 
    
     @throws FileNotFoundException If the input file name passed to the
                                   constructor cannot be found.
   */
  public void openForInput () 
      throws FileNotFoundException, IOException {
    openAsOutput = false;
    openAsInput = false;
    atEnd = true;
    if (isURL) {
      urlIn = url.openStream();
      urlReader = new InputStreamReader (urlIn);
      textFileBufReader = new BufferedReader (urlReader);
    } else {
      if (! this.exists() ) {
        throw new FileNotFoundException (this.toString() + " does not exist.");
      }
      if (! this.isFile () ) {
        throw new FileNotFoundException (this.toString() + " is not a file.");
      }
      if (! this.canRead () ) {
        throw new FileNotFoundException (this.toString() + " cannot be read.");
      }
      textFileReader = new FileReader (this);
      textFileBufReader = new BufferedReader (textFileReader);
    }
    openAsInput = true;
    atEnd = false;
  }
  
  /**
     Indicates whether the file has reached its end.</p>
    
     @return    True if file is at end, false if there are more records to read.
   */
  public boolean isAtEnd() {
    return atEnd;
  }
  
  /**
     Writes a String to the text file. If the text file has not yet 
     been opened, then the first execution of this method will 
     automatically attempt to open the file for output. 
     
     @param inStr   The next block of text to be written to the text file.
    
     @throws IOException If there is trouble writing to the disk file.
   */
  public void write (String inStr) 
      throws IOException {
    line = inStr;
    if (! openAsOutput) {
      openForOutput();
    }
    if (openAsOutput) {
      textFileBufWriter.write(line, 0, line.length());
    } // end if openAsOutput
    return;
  } // end method writeLine
  
  /**
     Writes the next line to the text file. If the text file has not yet 
     been opened, then the first execution of this method will 
     automatically attempt to open the file for output. 
     
     @param inStr   The next line to be written to the text file.
    
     @throws IOException If there is trouble writing to the disk file.
   */
  public void writeLine (String inStr) 
      throws IOException {
    line = inStr;
    if (! openAsOutput) {
      openForOutput();
    }
    if (openAsOutput) {
      textFileBufWriter.write(line, 0, line.length());
      String lineSep = System.getProperty(LINE_SEP);
      textFileBufWriter.write (lineSep, 0, lineSep.length());
      lineNumber++;
    } // end if openAsOutput
    return;
  } // end method writeLine
  
  /**
     Opens the text file for output. Note that this method 
     need not be explictly executed, since the first execution
     of writeLine will cause this method to be invoked automatically. 
    
     @throws IO Exception If there is trouble opening the disk file.
   */
  public void openForOutput () 
      throws IOException {
    openAsOutput = false;
    openAsInput = false;
    if (this.isDirectory () ) {
      throw new IOException (this.toString() + " is a directory.");
    }
    File parent = getParentFile();
    // System.out.println ("Opening " + toString() + " as output");
    /*
    if (parent.exists()) {
      System.out.println ("Parent " + parent.toString() + " exists");
    } else {
      System.out.println ("Parent " + parent.toString() + " does not exist");
    }
     */
    if (parent != null) {
      if (! parent.exists()) {
        boolean ok = parent.mkdirs();
        // System.out.println ("mkdirs result = " + String.valueOf (ok));
      }
    }
    textFileWriter = new FileWriter (this);
    textFileBufWriter = new BufferedWriter (textFileWriter);
    openAsOutput = true;
  } // end method openForOutput
  
  /**
     Closes the file, if it is currently open for input or output. 
    
     @throws IOException If there is trouble closing the disk file.
   */  
  public void close() 
      throws IOException {
    if (openAsInput) {
      textFileBufReader.close ();
    }
    if (openAsOutput) {
      textFileBufWriter.close();
    }
    if (openAsInput) {
      openAsInput = false;
      atEnd = true;
    }
    if (openAsOutput) {
      openAsOutput = false;
    }
  } // end method close
  
  /**
     Returns the file name, without any path info.
    
     @return  The name of the file, without any path info.
   */
  public String getFileName() {
    return fileName;
  }
  
  /**
     Returns the file path and name.
    
     @return  The name of the file, including all path information.
   */
  public String getFilePathAndName() {
    return filePathAndName;
  }
  
  /**
     Returns the number of the last line read from or written to the file.
     
     @return  The line number of the last line read or written.
   */
  public int getLineNumber() {
    return lineNumber;
  } 
  
} // end class XTextFile

