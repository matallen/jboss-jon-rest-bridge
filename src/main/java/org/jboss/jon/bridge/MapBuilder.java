package org.jboss.jon.bridge;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;

public class MapBuilder<K,V> {
  private Map<K, V> params = Maps.newHashMap();
  
  public static <K,V> MapBuilder<K,V> builder(){return new MapBuilder<K,V>();}
  
  public MapBuilder<K,V> with(K key, V value) {
    params.put(key, value);
    return this;
  }

  public Map<K,V> build() {
    HashMap<K,V> result = new HashMap<K,V>(params.size());
    for (Map.Entry<K,V> e : params.entrySet())
      result.put(e.getKey(), e.getValue());
    return result;
  }
}
