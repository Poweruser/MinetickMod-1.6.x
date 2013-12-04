package net.minecraft.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class IntCache {

    /*
    private static int a = 256;
    private static List b = new ArrayList();
    private static List c = new ArrayList();
    private static List d = new ArrayList();
    private static List e = new ArrayList();
    */
    // Poweruser start - no more static
    private int a = 256;
    private List<int[]> b = new ArrayList<int[]>();
    private List<int[]> c = new ArrayList<int[]>();
    private List<int[]> d = new ArrayList<int[]>();
    private List<int[]> e = new ArrayList<int[]>();
    private static List<IntCache> allCaches = Collections.synchronizedList(new LinkedList<IntCache>());

    public IntCache() {
        allCaches.add(this);
    }
    // Poweruser end

    //public static synchronized int[] a(int i) {
    public synchronized int[] a(int i) { // Poweruser - no more static
        int[] aint;

        if (i <= 256) {
            if (b.isEmpty()) {
                aint = new int[256];
                c.add(aint);
                return aint;
            } else {
                aint = (int[]) b.remove(b.size() - 1);
                c.add(aint);
                return aint;
            }
        } else if (i > a) {
            a = i;
            d.clear();
            e.clear();
            aint = new int[a];
            e.add(aint);
            return aint;
        } else if (d.isEmpty()) {
            aint = new int[a];
            e.add(aint);
            return aint;
        } else {
            aint = (int[]) d.remove(d.size() - 1);
            e.add(aint);
            return aint;
        }
    }

    //public static synchronized void a() {
    public synchronized void a() { // Poweruser - no more static
        if (!d.isEmpty()) {
            d.remove(d.size() - 1);
        }

        if (!b.isEmpty()) {
            b.remove(b.size() - 1);
        }

        d.addAll(e);
        b.addAll(c);
        e.clear();
        c.clear();
    }

    public static String b() {
        // Poweruser start
        int d = 0, b = 0, e = 0, c = 0;
        for(IntCache ic: allCaches) {
            d += ic.d.size();
            b += ic.b.size();
            e += ic.e.size();
            c += ic.c.size();
        }
        return "cache: " + d + ", tcache: " + b + ", allocated: " + e + ", tallocated: " + c;
        // Poweruser end
    }
}
