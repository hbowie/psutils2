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

  import com.powersurgepub.psutils2.textio.*;

  import java.io.*;

/**
  An interface for writing nodes to an output data store. 

  @author hbowie
 */
public interface NodeWriter {
  
  /**
  Set the file to be used for output.
  
  @param file The file to be written to. 
  */
  public void setFile(File file);
  
  public String getFileExt();
  
  public TextLineWriter getTextLineWriter();
  
  public boolean openForOutput();
  
  public boolean writeComment(String comment);
  
  public boolean writeNode(String name, String data);
  
  public boolean writeAttribute(String name, String data);
  
  public boolean startNodeOut(String name);
  
  public boolean startNodeOut(String name, boolean isAttribute);
  
  public boolean writeData(String name);
  
  public boolean writeData(String data, boolean isAttribute);
  
  public boolean endNodeOut(String name);
  
  public boolean endNodeOut(String name, boolean isAttribute);
  
  public boolean close();

  
}
