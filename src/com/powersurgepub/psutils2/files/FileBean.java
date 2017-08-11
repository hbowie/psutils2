/*
 * Copyright 2017 Herb Bowie
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

package com.powersurgepub.psutils2.files;

  import java.io.*;
  import java.text.*;
  import java.util.*;

  import javafx.beans.property.*;

/**
 A JavaFX Bean to provide table view data of a File 

 @author Herb Bowie
 */
public class FileBean {
  
  private StringProperty name 
      = new SimpleStringProperty(this, "name", "");
  private StringProperty date 
      = new SimpleStringProperty(this, "lastModDate", "");
  private StringProperty exists 
      = new SimpleStringProperty(this, "exists", " ");
  
  private SimpleDateFormat format 
      = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
  
  private File file;
  
  public FileBean() {
    
  }
  
  public FileBean(File file) {
    setFile(file);
  }
  
  public void setFile(File file) {
    this.file = file;
    name.set(file.getName());
    
    if (file != null && file.exists()) {
      Date lastMod = new Date(file.lastModified());
      date.set (format.format(lastMod)); 
    } else {
      date.set(" ");
    }
    
    exists.set(String.valueOf(file.exists()));
  }
  
  public File getFile() {
    return file;
  }
  
  public final String getName() {
    return name.get();
  }
  
  public final void setName(String name) {
    this.name.set(name);
  }
  
  public final String getDate() {
    return date.get();
  }
  
  public final void setDate(String lastModDate) {
    this.date.set(lastModDate);
  }
  
  public final String getExists() {
    return exists.get();
  }
  
  public final void setExists(String exists) {
    this.exists.set(exists);
  }

}
