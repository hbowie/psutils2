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

package com.powersurgepub.psutils2.records;

/**
 This object represents an album known to iTunes. 

 @author Herb Bowie
 */
public class ItunesAlbum {

  public static final String VARIOUS = "Various";

  private String title = "";
  private String artist = "";
  private String sortArtist = "";
  private String genre = "";
  private String year = "";
  private int    unratedTracks = 0;
  private int    hiFiTracks = 0;
  private int    loFiTracks = 0;
  private int    nonPodcastTracks = 0;
  private String loFiType = "";

  public ItunesAlbum () {

  }
  
  public ItunesAlbum (String title) {
    this.title = title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getTitle () {
    return title;
  }

  public void setArtist (String artist) {
    // System.out.println ("Setting artist for album " + title + " to (" + artist + ")");
    if (this.artist.length() == 0) {
      this.artist = artist;
    }
    else
    if (this.artist.equalsIgnoreCase(artist)) {
      // Same artist -- ok as is
    } else {
      this.artist = "Various";
    }
  }

  public String getArtist() {
    return artist;
  }

  public void setSortArtist (String sortArtist) {
    if (this.sortArtist.length() == 0) {
      this.sortArtist = sortArtist;
    }
    else
    if (this.sortArtist.equalsIgnoreCase(sortArtist)) {
      // Same artist -- ok as is
    } else {
      this.sortArtist = "Various";
    }
  }

  public String getSortArtist () {
    if (sortArtist.length() == 0
        || artist.equals(VARIOUS)) {
      return artist;
    } else {
      return sortArtist;
    }
  }

  public void setGenre (String genre) {
    String genreLower = genre.toLowerCase();
    String normalizedGenre;
    if (genreLower.contains("holiday")
        || genreLower.contains("xmas")
        || genreLower.contains("christmas")) {
      normalizedGenre = "Holiday";
    }
    else
    if (genreLower.contains("jazz")) {
      normalizedGenre = "Jazz";
    } else {
      normalizedGenre = genre;
    }
    if (this.genre.length() == 0) {
      this.genre = normalizedGenre;
    }
    else
    if (this.genre.equalsIgnoreCase(normalizedGenre)) {
      // Same genre -- ok as is
    } else {
      this.genre = "Mixed";
    }
  }

  public String getGenre () {
    return genre;
  }

  public void setYear (String year) {
    if (year.compareTo(this.year) > 0) {
      this.year = year;
    }
  }

  public String getYear () {
    return year;
  }

  public void countUnratedTracks() {
    unratedTracks++;
  }

  public int getUnratedTracks() {
    return unratedTracks;
  }
  
  public void countNonPodcastTracks() {
    nonPodcastTracks++;
  }
  
  public int getNonPodcastTracks() {
    return nonPodcastTracks;
  }

  public void countFidelityTracks(String kind) {

    if (kind.toLowerCase().contains("lossless")) {
      hiFiTracks++;
    } else {
      loFiTracks++;

      String displayKind;
      if (kind.contains ("AAC")) {
        displayKind = "AAC";
      }
      else
      if (kind.startsWith("MPEG")) {
        displayKind = "MP3";
      } else {
        displayKind = kind;
      }

      if (loFiType.equals("")) {
        loFiType = displayKind;
      }
      else
      if (loFiType.equals(displayKind)) {
        // leave as is
      } else {
        loFiType = "Mixed";
      }
    }

  }

  public int getLoFiTracks() {
    return loFiTracks;
  }

  public int getHiFiTracks() {
    return hiFiTracks;
  }

  public int getTracks () {
    if (hiFiTracks > loFiTracks) {
      return hiFiTracks;
    } else {
      return loFiTracks;
    }
  }

  public String getLoFiType () {
    return loFiType;
  }

}
