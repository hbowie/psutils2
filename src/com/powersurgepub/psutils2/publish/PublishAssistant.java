/*
 * Copyright 2011 - 2013 Herb Bowie
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

package com.powersurgepub.psutils2.publish;

  import java.io.*;

/**
 An interface for a class that assists in publishing data via PublishWindow.
 
 @author Herb Bowie
 */
public interface PublishAssistant {

  /**
   Prepare data to be used in the publishing process by PublishWindow.

   @param publishTo The folder to which we are publishing.
   */
  public void prePub(File publishTo);

  /**
   Perform a publishing operation when requested by the publishing script.

   @param publishTo The folder to which we are publishing.
   @param operand An operand specifying the operation to
                  be performed.
   */
  public boolean pubOperation(File publishTo, String operand);

  /**
   Any post-processing to be done after PublishWindow has completed its
   publication process.

   @param publishTo The folder to which we are publishing. 
   */
  public void postPub(File publishTo);

}
