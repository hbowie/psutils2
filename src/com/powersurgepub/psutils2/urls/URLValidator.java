/*
 * Copyright 2004 - 2013 Herb Bowie
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

package com.powersurgepub.psutils2.urls;

  import java.io.*;
  import java.net.*;

/**
   A web page with a URL and the ability to validate its existence. 
  
 */
public class URLValidator
    extends Thread {

  private             URLValidationRegistrar registrar;
  private             ItemWithURL            item;
  private             int                    index = -1;
  private             URL                    url;
  private             int                    status = UNKNOWN;
  public static final int                    UNKNOWN = 0;
  public static final int                    VALID   = 1;
  public static final int                    INVALID = -1;
  private             String                 error = "";
  
  /** 
    Creates a new instance of WebPage 
   */
  public URLValidator (
      ThreadGroup group,
      ItemWithURL item,
      int index,
      URLValidationRegistrar registrar) {
    super (group, item.getURLasString());
    this.item = item;
    this.index = index;
    this.registrar = registrar;
  }
  
  /**
    Validate this URL.
   */
  public void run() {

    try {
      url = new URL (item.getURLasString());
    } catch (MalformedURLException e) {
      status = INVALID;
      error = "Malformed URL";
    }
    if (status == UNKNOWN) {
      try {
        URLConnection handle = url.openConnection();
        if (url.getProtocol().equals ("http")) {
          HttpURLConnection httpHandle = (HttpURLConnection)handle;
          int response = httpHandle.getResponseCode();
          if (response == HttpURLConnection.HTTP_OK
              || response == HttpURLConnection.HTTP_MOVED_TEMP
              || response == HttpURLConnection.HTTP_FORBIDDEN
              || response == HttpURLConnection.HTTP_INTERNAL_ERROR) {
            status = VALID;
          } else {
            status = INVALID;
            error = "HTTP Response " + String.valueOf (response)
                + httpHandle.getResponseMessage();
          }
        } 
        else
        if (url.getProtocol().equals ("file")) {
          InputStream file = handle.getInputStream();
          file.close();
          status = VALID;
        } else {
          status = VALID;
        }
      }
      catch (SocketException e) {
        status = INVALID;
        error = "SocketException";
      }
      catch (IOException e) {
        status = INVALID;
        error = "IOException";
      }
      catch (Exception e) {
        status = INVALID;
        error = "Exception";
      }
    } // end if status not yet determined

    registrar.registerURLValidationResult (item, (status == VALID));
  } // end run method
  
  public boolean isValidationComplete () {
    return (status != UNKNOWN);
  }
  
  public boolean isInvalidURL () {
    return (status == INVALID);
  }

  public boolean isValidURL () {
    return (status == VALID);
  }

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
