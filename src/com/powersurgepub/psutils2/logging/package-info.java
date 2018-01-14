/**
 * A package for recording data and events to a log. 
 * <p>
 * LogOutput is a basic class for writing log lines to System.out. Other valid
 * output classes extend this basic class. 
 * <p>
 * LogOutputDisk extends LogOutput and provides the ability to log to a disk
 * file. 
 * <p>
 * LogOutputSysOut extends LogOutput and adds nothing special, since LogOutput
 * already logs to System.out. 
 * <p>
 * LogOutputNone extends LogOutput, and overrides the writeLine method to
 * throw away whatever lines are passed. 
 * <p>
 * LogWindow extends LogOutput, and overrides the writeLine method to
 * write output to a text area within a separate window. 
 * <p>
 * Logger is the actual class that writes to the LogOutput. It uses the 
 * Singleton pattern to offer a single instance of itself that can be shared
 * by all other classes that might need to do logging. This class contains 
 * a number of options to control how and when data and events are 
 * written to the log. 
 * <p>
 * LogEvent represents an event to be recorded to the log. An event has a
 * Severity and a message. 
 * <p>
 * LogData represents a line of data to be written to the log. 
 * <p>
 * Copyright 1999 - 2017 Herb Bowie
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.powersurgepub.psutils2.logging;
