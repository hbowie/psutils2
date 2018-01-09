/*
 * Copyright 2013 - 2015 Herb Bowie
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

/**
 A generic interface for a utility that can tweak a URL. 

 @author Herb Bowie
 */
public interface LinkTweakerInterface {
  
  /**
   Set the link URL along with an identifying String. 
  
   @param passedLink The URL to be tweaked.
  
   @param LinkID A string identifying which URL is being passed. 
  */
  public void setLink(String passedLink, String LinkID);
  
  /**
   Set the location of the link tweaker window. 
  
   @param x The desired horizontal position of the top left corner. 
   @param y The desired vertical position of the top left corner. 
  */
  public void setLocation (double x, double y);
  
}
