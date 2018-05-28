package com.jsen.test;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/27
 */
public class TestMap {
    private class Test {
        String path;

        public Test(String path) {
            this.path = path;
        }


        @Override
        public String toString() {
            return path;
        }


        @Override
        public boolean equals(Object obj) {
            return this.path.equals(obj.toString());
        }
    }

    private class Item{
        private String path;

        public Item(String path) {
            this.path = path;
        }

        @Override
        public String toString() {
            return path;
        }
        public boolean equals(Object o) {
            return path.equals(o.toString());
        }
    }



    @org.junit.Test
    public void test() {
        List<Test> tests = Lists.newArrayList();
        tests.add(new Test("Hello"));
        tests.add(new Test("hello"));
        tests.add(new Test("Hello2"));
        tests.add(new Test("Hello3"));
        System.out.println(tests.size());
        tests.remove("Hello");
        tests.remove(new Item("Hello"));
        System.out.println(tests.size());
    }
}
