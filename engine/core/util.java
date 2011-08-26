package core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Utility methods to use across all classes.
 */
public class util {

    public static <T> Set<T> mkSet(T... args) {
        Set<T> set = new HashSet<T>();
        for (T t : args) {
            set.add(t);
        }
        return set;
    }

    public static <T> List<T> mkList(T... args) {
        List<T> list = new ArrayList<T>();
        for (T t : args) {
            list.add(t);
        }
        return list;
    }

    public static Map<Integer, Float> mkMap(double[] arg[]) {
        Map<Integer, Float> map = new HashMap<Integer, Float>();
        for (double[] i : arg)
            map.put((int) i[0], (float) i[1]);
                return map;
    }

    public static <T> HashSet<T> addSets(Set<T>... args) {
        HashSet<T> set = new HashSet<T>();
        for (Set<T> s : args) {
            for (T t : s) {
                set.add(t);
            }
        }
        return set;
    }

    public static <T> List<T> addLists(ArrayList<T>... args) {
        List<T> list = new ArrayList<T>();
        for (List<T> l : args) {
            list.addAll(l);
        }
        return list;
    }

    public static int sumArray(int[] arg) {
        int sum = 0;
        for (int i : arg) {
            sum += i;
        }
        return sum;
    }

    public static double sumArray(double[] arg) {
        double sum = 0;
        for (double d : arg) {
            sum += d;
        }
        return sum;
    }

    public static double sumArray(Double[] arg) {
        double sum = 0;
        for (Double d : arg) {
            sum += (d != null) ? d : 0;
        }
        return sum;
    }

    public static Map<Object, Object> cloneMap(HashMap<Object, Object> arg) {
        Map<Object, Object> map = new HashMap<Object, Object>();
        for (Object o : arg.keySet()) {
            map.put(o, arg.get(o));
        }
        return map;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static List sortByValue(final Map<?, ?> arg, final boolean reverse) {
        List keys = new ArrayList();
        keys.addAll(arg.keySet());
        Collections.sort(keys, new Comparator() {
            // @Override
            public int compare(Object o1, Object o2) {
                Object v1 = (reverse) ? arg.get(o2) : arg.get(o1);
                Object v2 = (reverse) ? arg.get(o1) : arg.get(o2);

                if (v1 == null)
                    return (v2 == null) ? 0 : 1;
                else if (v1 instanceof Comparable)
                    return ((Comparable) v1).compareTo(v2);
                else
                    return 0;
            }
        });
        return keys;
    }

    @SuppressWarnings("rawtypes")
    public static List sortByValue(final Map arg) {
        return sortByValue(arg, false);
    }

    public static void pretty_print(double total_dps, Map<?, ?>... maps) {
        int offset = 2;
        int value_length = 18;

        int length = 0;
        for (Map<?, ?> map : maps) {
            for (Entry<?, ?> item : map.entrySet()) {
                int i = ((String) item.getKey()).length();
                length = (i > length) ? i : length;
            }
        }
        for (int i = -value_length - offset; i < length; i++) {
            System.out.print("-");
        }
        System.out.println("");
        for (Map<?, ?> map : maps) {
            for (Iterator<?> i = util.sortByValue(map, true).iterator(); i.hasNext();) {
                String key = (String) i.next();
                String value = map.get(key).toString();
                if (value.equals("-1.0"))
                    value = "-not modeled-";
                System.out.print(key);
                for (int j = -offset; j < (length - key.length()); j++) {
                    System.out.print(" ");
                }
                System.out.println(value);
            }
            for (int i = -value_length - offset; i < length; i++) {
                System.out.print("-");
            }
            System.out.println("");
        }
        for (int i = -offset; i < length; i++) {
            System.out.print(" ");
        }
        System.out.println(total_dps + " total damage per second.");
    }

    public static void printDist(HashMap<Integer[], Double> dist) {
        System.out.print("{");
        for (Entry<Integer[], Double> item : dist.entrySet()) {
            double key0 = item.getKey()[0];
            double key1 = item.getKey()[1];
            double value = item.getValue();
            System.out.print("(" + key0 + ", " + key1 + "): " + value + ", ");
        }
        System.out.println("}");
    }

    public static void printAPS(HashMap<String, Double[]> aps) {
        System.out.print("{");
        for (Entry<String, Double[]> item : aps.entrySet()) {
            System.out.print("" + item.getKey() + ":[");
            for (double d : item.getValue()) {
                System.out.print(d + ", ");
            }
            System.out.print("], ");
        }
        System.out.println("}");
    }

    public static <T> void printArray(T[] arg) {
        List<T> list = Arrays.asList(arg);
        System.out.println(list);
    }

    public static void printArray(double[] arg) {
        List<Double> list = new ArrayList<Double>();
        for (double element : arg) {
            list.add(element);
        }
        System.out.println(list);
    }

    public static class Tuple_2<A, B> extends util {
        final A first;
        final B second;

        public Tuple_2(A first, B second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other)
                return true;
            else if (other != null || other instanceof Tuple_2) {
                @SuppressWarnings("unchecked")
                Tuple_2<A, B> otherTuple = (Tuple_2<A, B>) other;
                boolean check_first = this.first == otherTuple.first || (this.first != null && this.first.equals(otherTuple.first));
                boolean check_second = this.second == otherTuple.second || (this.second != null && this.second.equals(otherTuple.second));
                return check_first && check_second;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hashFirst = (this.first != null) ? this.first.hashCode() : 0;
            int hashSecond = (this.second != null) ? this.second.hashCode() : 0;
            final int prime = 31;
            int seed = 1;
            for (int i : new int[] {hashFirst, hashSecond}) {
                seed = prime * seed + i;
            }
            return seed;
        }

        @Override
        public String toString() {
            return "(" + this.first + ", " + this.second + ")";
        }

        public A getFirst() {
            return this.first;
        }

        public B getSecond() {
            return this.second;
        }
    }

}
