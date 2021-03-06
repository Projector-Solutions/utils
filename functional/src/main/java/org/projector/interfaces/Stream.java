package org.projector.interfaces;

import java.util.List;
import java.util.Map;

import org.projector.types.Duet;

public interface Stream<ValueType> {    
    public <IteratorType extends StreamIterator<ValueType>> IteratorType iterate();
    
    public void foreach(VoidConsumer<ValueType> consumer);

    public void foreach(Consumer<ValueType, ValueType> consumer);

    public <OutType> Stream<OutType> select(Selector<ValueType, OutType> selector);

    public <KeyType> Map<KeyType, ValueType> map(Selector<ValueType, KeyType> selector);

    public Stream<ValueType> where(Consumer<ValueType, Boolean> consumer);

    public <OutType> Stream<OutType> group(Consumer<Duet<ValueType, OutType>, OutType> groupEditor,
            Consumer<ValueType, Integer> consumer);

    public boolean any(Consumer<ValueType, Boolean> consumer);

    public boolean all(Consumer<ValueType, Boolean> consumer);

    public Float sumFloat(Consumer<ValueType, Float> consumer);

    public Double sumDouble(Consumer<ValueType, Double> consumer);

    public Integer sumInt(Consumer<ValueType, Integer> consumer);

    public Long sumLong(Consumer<ValueType, Long> consumer);

    public Double average(Consumer<ValueType, Double> consumer);

    public ValueType max(Consumer<ValueType, Double> consumer);

    public ValueType min(Consumer<ValueType, Double> consumer);

    public Duet<ValueType, Double> maxDuet(Consumer<ValueType, Double> consumer);

    public Duet<ValueType, Double> minDuet(Consumer<ValueType, Double> consumer);

    public List<ValueType> toList();
    
    public boolean isMutable();
    
    public void setMutable(boolean mutable);
    
    public ValueType get(int index);
    
    public void set(int index, ValueType value);
    
    public ValueType remove(int index);
    
    public boolean remove(int index, ValueType value);
}
