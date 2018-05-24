package com.jsen.test;

import com.jsen.joker.boot.entity.Entry;
import com.jsen.joker.boot.joker.context.EntryManager;
import org.junit.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/23
 */
public class EntryManagerTest {
    @Test
    public void test() {
        EntryManager entryManager = new EntryManager();
        entryManager.createEntry("", "", "", "", "", null, false, 4);
        entryManager.createEntry("", "", "", "", "", null, false, 5);
        entryManager.createEntry("", "", "", "", "", null, false, 1);
        entryManager.createEntry("", "", "", "", "", null, false, 2);
        entryManager.createEntry("", "", "", "", "", null, false, 3);

        for (Entry entry:entryManager.getEntryList()) {
            System.out.println(entry.getPriority());
        }

        System.out.println();
        Map<Integer, List<Entry>> listMap =   entryManager.getEntryList().stream().collect(Collectors.groupingBy(Entry::getPriority));


        TreeMap<Integer, List<Entry>> ts = new TreeMap<>(Comparator.reverseOrder());
        ts.putAll(listMap);

        for (Map.Entry<Integer, List<Entry>> entry:ts.entrySet()) {
            System.out.println(entry.getKey());
        }
        System.out.println();
        entryManager.sort();
        listMap =   entryManager.getEntryList().stream().collect(Collectors.groupingBy(Entry::getPriority));
        ts.clear();
        ts.putAll(listMap);

        for (Entry entry:entryManager.getEntryList()) {
            System.out.println(entry.getPriority());
        }
        System.out.println();
        for (Map.Entry<Integer, List<Entry>> entry:ts.entrySet()) {
            System.out.println(entry.getKey());
        }
    }
}
