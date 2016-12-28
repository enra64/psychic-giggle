package de.ovgu.softwareprojekt.util;

import java.util.ArrayList;

/**
 * This is a ring buffer implementation. It is, however, severely limited in its capabilities.
 * <p>
 * Supported operations include:<br>
 * * {@link #add(Object)}: overwrite the oldest element<br>
 * * {@link #get(int)}: get item at specific index<br>
 * * {@link #size()} get the amount of elements currently stored in the container
 */
public class RingBuffer<E> {
    /**
     * Private element storage
     */
    private ArrayList<E> mBuffer;

    /**
     * Where the next element should be written to
     */
    private int mIndex = 0;

    /**
     * How many elements are stored in this container
     */
    private int mSize = 0;

    /**
     * Construct a new {@link RingBuffer} with a capacity of 64.
     */
    public RingBuffer() {
        this(64);
    }

    /**
     * Construct a new {@link RingBuffer} with the specified capacity
     *
     * @param size final size of this container
     */
    public RingBuffer(int size) {
        mBuffer = new ArrayList<>(size);
    }

    /**
     * Construct a new {@link RingBuffer} with the specified capacity
     *
     * @param size     final size of this container
     * @param fillWith an element that should be in every point of this container
     */
    public RingBuffer(int size, E fillWith) {
        this(size);
        for (int i = 0; i < size; i++)
            mBuffer.add(fillWith);
        mSize = size;
    }

    /**
     * Get an item. Index 0 will always retrieve the newest item, while index (size - 1) will retrieve the oldest item.
     */
    public E get(int index) {
        int timedIndex = mIndex - 1 + index;

        if(timedIndex < 0)
            timedIndex += mBuffer.size();
        else
            timedIndex %= mBuffer.size();

        return mBuffer.get(timedIndex);
    }

    /**
     * Add a new element, overwriting the oldest one. The overwritten element is returned.
     *
     * @param element the new element
     * @return the overwritten element
     */
    public E add(E element) {
        // update the size variable
        if (mSize < mBuffer.size())
            mSize++;

        E tmp = mBuffer.get(mIndex);

        // set the element
        mBuffer.set(mIndex++, element);

        // wrap around the buffer positions
        if (mIndex >= mBuffer.size())
            mIndex = 0;

        return tmp;
    }

    public int size() {
        return mSize;
    }
}
