package com.tideengine;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.Vector;


public class Stations implements Serializable
{
  @SuppressWarnings("compatibility:8783605059833839037")
  private final static long serialVersionUID = 1L;
  
  private Map<String, TideStation> stations = new HashMap<String, TideStation>();
  
  public Stations()
  {
  }

  public Stations(Map<String, TideStation> stations)
  {
    this.stations = stations;
  }

  public Map<String, TideStation> getStations()
  {
    return stations;
  }
}
