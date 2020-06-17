package com.cache;

import java.util.ArrayList;
import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.map.LRUMap;

 
public class CacheInMemoryCache<K, T> {
 
    private long timeToLive;
    private LRUMap sampleCacheMap;
 
    protected class CacheObject {
        public long lastAccessed = System.currentTimeMillis();
        public T value;
 
        protected CacheObject(T value) {
            this.value = value;
        }
    }
 
    public CacheInMemoryCache(long sampleTimeToLive, final long sampleTimerInterval, int maxItems) {
        this.timeToLive = sampleTimeToLive * 1000;
 
        sampleCacheMap = new LRUMap(maxItems);
 
        if (timeToLive > 0 && sampleTimerInterval > 0) {
 
            Thread t = new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(sampleTimerInterval * 1000);
                        } catch (InterruptedException ex) {
                        }
                        cleanup();
                    }
                }
            });
 
            t.setDaemon(true);
            t.start();
        }
    }
 
    public void put(K key, T value) {
        synchronized (sampleCacheMap) {
            sampleCacheMap.put(key, new CacheObject(value));
        }
    }
 
    @SuppressWarnings("unchecked")
    public T get(K key) {
        synchronized (sampleCacheMap) {
        	CacheObject c = (CacheObject) sampleCacheMap.get(key);
 
            if (c == null)
                return null;
            else {
                c.lastAccessed = System.currentTimeMillis();
                return c.value;
            }
        }
    }
 
    public void remove(K key) {
        synchronized (sampleCacheMap) {
            sampleCacheMap.remove(key);
        }
    }
 
    public int size() {
        synchronized (sampleCacheMap) {
            return sampleCacheMap.size();
        }
    }
 
    @SuppressWarnings("unchecked")
    public void cleanup() {
 
        long now = System.currentTimeMillis();
        ArrayList<K> deleteKey = null;
 
        synchronized (sampleCacheMap) {
            MapIterator itr = sampleCacheMap.mapIterator();
 
            deleteKey = new ArrayList<K>((sampleCacheMap.size() / 2) + 1);
            K key = null;
            CacheObject c = null;
 
            while (itr.hasNext()) {
                key = (K) itr.next();
                c = (CacheObject) itr.getValue();
 
                if (c != null && (now > (timeToLive + c.lastAccessed))) {
                    deleteKey.add(key);
                }
            }
        }
 
        for (K key : deleteKey) {
            synchronized (sampleCacheMap) {
                sampleCacheMap.remove(key);
            }
 
            Thread.yield();
        }
    }
}
