package org.projector.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.projector.interfaces.MutableStreamIterator;
import org.projector.interfaces.Stream;
import org.projector.interfaces.StreamIterator;
import org.projector.types.Duet;

public class DefaultStreamTest {
    
    @Test
    @SuppressWarnings("unused")
    public void testIterateImmutableCorrect() {
        DefaultStream<String> stream = new DefaultStream<>(Collections.emptyList());
        stream.setMutable(false);
        StreamIterator<String> immutableIterator = stream.iterate();

        stream.setMutable(true);
        MutableStreamIterator<String> mutableIterator = stream.iterate();
    }
    
    @Test(expected = ClassCastException.class)
    @SuppressWarnings("unused")
    public void testIterateImmutableFail() {
        DefaultStream<String> stream = new DefaultStream<>(Collections.emptyList());
        stream.setMutable(false);
        MutableStreamIterator<String> iterator = stream.iterate();
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetNoSuchElementEmpty() {
        DefaultStream<String> stream = new DefaultStream<>(Collections.emptyList());
        stream.get(0);
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetNoSuchElementNegative() {
        DefaultStream<Integer> stream = new DefaultStream<>(1, 2, 3, 4, 5, 6, 7, 8);
        stream.get(-4);
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetNoSuchElementOverLength() {
        DefaultStream<Integer> stream = new DefaultStream<>(1, 2, 3, 4, 5, 6, 7, 8);
        stream.get(8);
    }
    
    @Test
    public void testForeachVoid() {
        List<String> list = Arrays.asList("Hello", "world", "man");
        DefaultStream<String> stream = new DefaultStream<>(list);
        AtomicInteger counter = new AtomicInteger();

        stream.foreach(s -> {
            int i = counter.getAndIncrement();
            assertEquals(list.get(i), s);
        });

        assertEquals(3, counter.get());
    }

    @Test
    public void testForeachUpdate() {
        List<String> list = Arrays.asList("Hello", "world", "man");
        DefaultStream<String> stream = new DefaultStream<>(list);
        AtomicInteger counter = new AtomicInteger();

        stream.foreach(s -> {
            int i = counter.getAndIncrement();
            assertEquals(list.get(i), s);

            return s + "/" + s;
        });

        assertEquals(3, counter.get());
        counter.set(0);

        stream.foreach(s -> {
            int i = counter.getAndIncrement();
            String v = list.get(i);
            assertEquals(v + "/" + v, s);
        });
    }

    @Test
    public void testSelect() {
        List<String> list = Arrays.asList("Hello", "world", "man", "and girls");
        DefaultStream<String> stream = new DefaultStream<>(list);
        Stream<Integer> result = stream.select((s, l) -> {

            if (s.equals("and girls")) {
                l.stop();
            }

            return s.length();
        });

        AtomicInteger counter = new AtomicInteger();

        result.foreach(c -> {
            int i = counter.getAndIncrement();
            Integer l = list.get(i).length();
            assertEquals(l, c);
        });

        assertEquals(3, counter.get());
    }

    @Test
    public void testMap() {
        List<String> list = Arrays.asList("Hello", "world", "man");
        DefaultStream<String> stream = new DefaultStream<>(list);
        Map<Integer, String> result = stream.map((s, l) -> {

            if (s.equals("world")) {
                l.skip();
            }

            return s.length();
        });

        assertTrue(result.containsKey(5));
        assertFalse(result.containsKey(4));
        assertTrue(result.containsKey(3));

        assertEquals("Hello", result.get(5));
        assertEquals("man", result.get(3));
    }

    @Test
    public void testWhere() {
        List<String> list = Arrays.asList("Hello", "world", "man");
        DefaultStream<String> stream = new DefaultStream<>(list);
        Stream<String> result = stream.where(s -> s.equals("world"));

        AtomicInteger counter = new AtomicInteger();

        result.foreach(c -> {
            counter.getAndIncrement();
            assertEquals("world", c);
        });

        assertEquals(1, counter.get());
    }

    @Test
    public void testGroup() {
        List<String> list = Arrays.asList("123456", "123", "12", "123456789");
        DefaultStream<String> stream = new DefaultStream<>(list);
        Stream<String[]> result = stream.group(duet -> {
            String[] array = duet.getB();

            if (array == null) {
                array = new String[2];
            }

            if (array[0] == null) {
                array[0] = duet.getA();
            } else {
                array[1] = duet.getA();
            }

            return array;
        }, v -> {
            return v.length() > 4 ? 1 : 0;
        });

        AtomicInteger counter = new AtomicInteger();

        result.foreach(c -> {

            if (counter.getAndIncrement() == 0) {
                assertEquals("123", c[0]);
                assertEquals("12", c[1]);
            } else {
                assertEquals("123456", c[0]);
                assertEquals("123456789", c[1]);
            }

        });

        assertEquals(2, counter.get());
    }

    @Test
    public void testAny() {
        DefaultStream<String> stream = new DefaultStream<>("Hello", "world", "man");
        assertTrue(stream.any(s -> s.equals("man")));
        assertFalse(stream.any(s -> s.equals("lol")));
    }

    @Test
    public void testAll() {
        DefaultStream<Integer> stream = new DefaultStream<>(17, 162, 15, 12, 87, 40, 13, 23);
        assertTrue(stream.all(s -> s > 10));
        assertFalse(stream.all(s -> s < 162));
    }

    @Test
    public void testSum() {
        DefaultStream<Integer> intStream = new DefaultStream<>(10, 20, 30, 40, 50);
        Integer intExpected = 150;
        assertEquals(intExpected, intStream.sumInt(v -> v));

        DefaultStream<Float> floatStream = new DefaultStream<>(1.5f, 2.25f, 3.35f);
        Float floatExpected = 7.1f;
        assertEquals(floatExpected, floatStream.sumFloat(v -> v));

        DefaultStream<Double> doubleStream = new DefaultStream<>(1.5, 2.25, 3.35);
        Double doubleExpected = 7.1;
        assertEquals(doubleExpected, doubleStream.sumDouble(v -> v));

        DefaultStream<Long> longStream = new DefaultStream<>(1753L, 213L, 371089L);
        Long longExpected = 373055L;
        assertEquals(longExpected, longStream.sumLong(v -> v));
    }

    @Test
    public void testAverage() {
        DefaultStream<Integer> intStream = new DefaultStream<>(10, 20, 30, 40, 50);
        Double expected = 30.0;
        assertEquals(expected, intStream.average(v -> v.doubleValue()));
    }

    @Test
    public void testMin() {
        DefaultStream<Integer> intStream = new DefaultStream<>(10, 20, 30, 40, 50, 7, 150);
        Integer expected = 7;
        assertEquals(expected, intStream.min(v -> v.doubleValue() * 8));
    }

    @Test
    public void testMinDuet() {
        DefaultStream<Integer> intStream = new DefaultStream<>(10, 20, 30, 40, 50, 7, 150);
        Duet<Integer, Double> result = intStream.minDuet(v -> v.doubleValue() * 8);
        assertEquals((Integer)7, result.getA());
        assertEquals((Double)56.0, result.getB());
    }


    @Test
    public void testMax() {
        DefaultStream<Integer> intStream = new DefaultStream<>(10, 20, 150, 30, 40, 50, 7);
        Integer expected = 150;
        assertEquals(expected, intStream.max(v -> v.doubleValue() * 2));
    }

    @Test
    public void testMaxDuet() {
        DefaultStream<Integer> intStream = new DefaultStream<>(10, 20, 30, 40, 50, 7, 150);
        Duet<Integer, Double> result = intStream.maxDuet(v -> v.doubleValue() * 2);
        assertEquals((Integer)150, result.getA());
        assertEquals((Double)300.0, result.getB());
    }
    
    @Test
    public void testVarArgsConstructor() {
        Integer[] array = new Integer[] { 10, 20, 150, 30, 40, 50, 7 };
    	DefaultStream<Integer> intStream = new DefaultStream<>(10, 20, 150, 30, 40, 50, 7);
		for (int i = 0; i < array.length; i++) {
			assertEquals(array[i], intStream.get(i));
		}
    }
    
    @Test
    public void testListConstructor() {
    	List<Integer> list = new ArrayList<>();
    	list.addAll(Arrays.asList(10, 20, 150, 30, 40, 50, 7));
    	
    	DefaultStream<Integer> intStream = new DefaultStream<>(list);
    	for (int i = 0; i < list.size(); i++) {
            assertEquals(list.get(i), intStream.get(i));
        }
    }
    
    @Test
    public void testIsMutable() {
    	DefaultStream<Integer> mutableStream = new DefaultStream<>(Arrays.asList(11, 12, 13), true);
		assertTrue(mutableStream.isMutable());
		
		DefaultStream<Integer> immutableStream = new DefaultStream<>(Arrays.asList(11, 12, 13), false);
		assertFalse(immutableStream.isMutable());
		
		mutableStream.setMutable(false);
		assertFalse(mutableStream.isMutable());
		
		immutableStream.setMutable(true);
		assertTrue(immutableStream.isMutable());
    }
    
    @Test(expected = IllegalStateException.class)
    public void testRemoveByIndexFail() {
    	DefaultStream<Integer> mutableStream = new DefaultStream<>(Arrays.asList(11, 12, 13), false);
    	mutableStream.remove(0);
    }
    
    @Test(expected = IllegalStateException.class)
    public void testRemoveByValueFail() {
    	DefaultStream<Integer> mutableStream = new DefaultStream<>(Arrays.asList(11, 12, 13), false);
    	mutableStream.remove(0, 11);
    }
    
    @Test(expected = NoSuchElementException.class)
    public void testRemoveNegativeIndex() {
        DefaultStream<Integer> mutableStream = new DefaultStream<>(Arrays.asList(11, 12, 13), false);
        mutableStream.remove(-1);
    }
    
    @Test(expected = NoSuchElementException.class)
    public void testRemoveOverLength() {
        DefaultStream<Integer> mutableStream = new DefaultStream<>(Arrays.asList(11, 12, 13), false);
        mutableStream.remove(4);
    }      
    
    @Test
    public void testRemoveByIndex() {
    	DefaultStream<Integer> mutableStream = new DefaultStream<>(Arrays.asList(11, 12, 13), true);
    	mutableStream.remove(1);
    	
    	assertEquals((Integer)11, mutableStream.get(0));
    	assertEquals((Integer)13, mutableStream.get(1));
    }
    
    @Test
    public void testRemoveByValue() {
    	DefaultStream<Integer> mutableStream = new DefaultStream<>(Arrays.asList(11, 12, 13), true);
    	assertTrue(mutableStream.remove(1, 12));
    	
    	assertEquals((Integer)11, mutableStream.get(0));
    	assertEquals((Integer)13, mutableStream.get(1));
    }
    
    @Test
    public void testRemoveNoValue() {
    	DefaultStream<Integer> mutableStream = new DefaultStream<>(Arrays.asList(11, 12, 13), true);
    	assertFalse(mutableStream.remove(1, 15));
    	
    	assertEquals((Integer)11, mutableStream.get(0));
    	assertEquals((Integer)12, mutableStream.get(1));
    	assertEquals((Integer)13, mutableStream.get(2));
    }
}
