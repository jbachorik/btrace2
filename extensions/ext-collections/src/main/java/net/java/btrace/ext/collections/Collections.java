/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.java.btrace.ext.collections;

import net.java.btrace.api.extensions.BTraceExtension;
import net.java.btrace.ext.Printer;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.annotation.Resource;
import net.java.btrace.api.extensions.runtime.Objects;

/*
 * Wraps the collections related BTrace utility methods
 * @since 1.2
 */
@BTraceExtension
public class Collections {

    @Resource
    private static Objects objs;

    // Create a new map
    public static <K, V> Map<K, V> newHashMap() {
        return new BTraceMap(new HashMap<K, V>());
    }

    public static <K, V> Map<K, V> newWeakMap() {
        return new BTraceMap(new WeakHashMap<K, V>());
    }

    public static <V> Deque<V> newDeque() {
        return new BTraceDeque(new ArrayDeque<V>());
    }

    public static <K, V> void putAll(Map<K, V> src, Map<K, V> dst) {
        if (isMap(src) && isMap(dst)) {
            src.putAll(dst);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static <K, V> void copy(Map<K, V> src, Map<K, V> dst) {
        if (isMap(src) && isMap(dst)) {
            dst.clear();
            dst.putAll(src);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static <V> void copy(Collection<V> src, Collection<V> dst) {
        if (isCollection(src) && isCollection(dst)) {
            dst.clear();
            dst.addAll(src);
        } else {
            throw new IllegalArgumentException();
        }
    }

    // get a particular item from a Map
    public static <K, V> V get(Map<K, V> map, Object key) {
        if (isMap(map)) {
            return map.get((K) key);
        } else {
            throw new IllegalArgumentException("not a btrace map");
        }
    }

    // check whether an item exists
    public static <K, V> boolean containsKey(Map<K, V> map, Object key) {
        if (isMap(map)) {
            return map.containsKey((K) key);
        } else {
            throw new IllegalArgumentException("not a btrace map");
        }
    }

    public static <K, V> boolean containsValue(Map<K, V> map, Object value) {
        if (isMap(map)) {
            return map.containsValue((V) value);
        } else {
            throw new IllegalArgumentException("not a btrace map");
        }
    }

    // put a particular item into a Map
    public static <K, V> V put(Map<K, V> map, K key, V value) {
        if (isMap(map)) {
            return map.put(key, value);
        } else {
            throw new IllegalArgumentException("not a btrace map");
        }
    }

    // remove a particular item from a Map
    public static <K, V> V remove(Map<K, V> map, Object key) {
        if (isMap(map)) {
            return map.remove((K) key);
        } else {
            throw new IllegalArgumentException("not a btrace map");
        }
    }

    // clear all items from a Map
    public static <K, V> void clear(Map<K, V> map) {
        if (isMap(map)) {
            map.clear();
        } else {
            throw new IllegalArgumentException("not a btrace map");
        }
    }

    public static <V> void clear(Deque<V> queue) {
        if (isQueue(queue)) {
            queue.clear();
        } else {
            throw new IllegalArgumentException("not a btrace deque");
        }
    }

    // return the size of a Map
    public static <K, V> int size(Map<K, V> map) {
        if (isMap(map)) {
            return map.size();
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static <K, V> boolean isEmpty(Map<K, V> map) {
        if (isMap(map)) {
            return map.isEmpty();
        } else {
            throw new IllegalArgumentException();
        }
    }

    // operations on collections
    public static <E> int size(Collection<E> coll) {
        if (isCollection(coll)) {
            return coll.size();
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static <E> boolean isEmpty(Collection<E> coll) {
        if (isCollection(coll)) {
            return coll.isEmpty();
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static <E> boolean contains(Collection<E> coll, Object obj) {
        if (isCollection(coll)) {
            for (E e : coll) {
                if (objs.compare(e, obj)) {
                    return true;
                }
            }
            return false;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static boolean contains(Object[] array, Object value) {
        for (Object each : array) {
            if (objs.compare(each, value)) {
                return true;
            }
        }
        return false;
    }

    public static <E> Object[] toArray(Collection<E> collection) {
        if (collection == null) {
            return new Object[0];
        } else {
            return collection.toArray();
        }
    }

    // operations on Deque
    public static <V> void push(Deque<V> queue, V value) {
        if (isQueue(queue)) {
            queue.push(value);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static <V> V poll(Deque<V> queue) {
        if (isQueue(queue)) {
            return queue.poll();
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static <V> V peek(Deque<V> queue) {
        if (isQueue(queue)) {
            return queue.peek();
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static <V> void addLast(Deque<V> queue, V value) {
        if (isQueue(queue)) {
            queue.addLast(value);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static <V> V peekFirst(Deque<V> queue) {
        if (isQueue(queue)) {
            return queue.peekFirst();
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static <V> V peekLast(Deque<V> queue) {
        if (isQueue(queue)) {
            return queue.peekLast();
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static <V> V removeLast(Deque<V> queue) {
        if (isQueue(queue)) {
            return queue.removeLast();
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static <V> V removeFirst(Deque<V> queue) {
        if (isQueue(queue)) {
            return queue.removeFirst();
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static void printMap(Map map) {
        if (isMap(map)) {
            synchronized (map) {
                Map<String, String> m = new HashMap<String, String>();
                Set<Map.Entry<Object, Object>> entries = map.entrySet();
                for (Map.Entry<Object, Object> e : entries) {
                    m.put(e.getKey().toString(), e.getValue().toString());
                }
                Printer.printStringMap(null, m);
            }
        } else {
            Printer.print(map != null ? map.toString() : null);
        }
    }

    private static <K, V> boolean isMap(Map<K, V> map) {
        return map instanceof BTraceMap || (map != null && map.getClass().getClassLoader() == null);
    }

    private static <V> boolean isQueue(Deque<V> queue) {
        return queue instanceof BTraceDeque || (queue != null && queue.getClass().getClassLoader() == null);
    }

    private static <V> boolean isCollection(Collection<V> coll) {
        return coll instanceof BTraceCollection || (coll != null && coll.getClass().getClassLoader() == null);
    }
}
