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

package com.powersurgepub.psutils2.links;

  import java.io.*;
  import java.net.*;
  import javafx.concurrent.*;

/**
   A web page with a URL and the ability to validate its existence. 
  
 */
public class URLValidator
    extends Task<Void> {

  private             ItemWithURL            item;
  private             int                    index = -1;
  private             URL                    url;
  
  /** 
    Creates a new instance of WebPage 
   */
  public URLValidator (
      ItemWithURL item,
      int index) {
    this.item = item;
    this.index = index;
  }
  

  @Override
  public Void call() 
      throws 
        Exception,
        IOException,
        MalformedURLException,
        SocketException {

    updateTitle("URL Validator for " + item.getURLasString());

    url = new URL (item.getURLasString());

    URLConnection handle = url.openConnection();
    if (url.getProtocol().equals ("http")) {
      HttpURLConnection httpHandle = (HttpURLConnection)handle;
      int response = httpHandle.getResponseCode();
      if (response == HttpURLConnection.HTTP_OK
          || response == HttpURLConnection.HTTP_MOVED_TEMP
          || response == HttpURLConnection.HTTP_FORBIDDEN
          || response == HttpURLConnection.HTTP_INTERNAL_ERROR) {
        // Keep going
      } else {
        throw new Exception("HTTP Response " + String.valueOf (response)
            + httpHandle.getResponseMessage());
      }
    } 
    else
    if (url.getProtocol().equals ("file")) {
      InputStream file = handle.getInputStream();
      file.close();
    } 

    updateProgress(1, 1);
    
    return null;

  } // end run method

  public ItemWithURL getItemWithURL () {
    return item;
  }

  public int getIndex () {
    return index;
  }
  
  public String toString() {
    return item.getURLasString ();
  }
  
}
