package de.ovgu.softwareprojekt.util;

import java.util.ArrayList;

/**
 * This is a ring buffer implementation. It is severely limited in its capabilities.
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
     * Construct a new {@link RingBuffer} with the specified capacity
     *
     * @param size     final size of this container
     * @param fillWith an element that should be in every point of this container
     */
    public RingBuffer(int size, E fillWith) {
        mBuffer = new ArrayList<>(size);
        for (int i = 0; i < size; i++)
            mBuffer.add(fillWith);
        mSize = size;
    }

    /**
     * Get an item. Index 0 will always retrieve the newest item, while index (size - 1) will retrieve the oldest item.
     *
     * @param index the age index of the item; index 0 is the newest item, index (size - 1) is the oldest item.
     * @return the value stored at the given age index
     */
    public E get(int index) {
        int timedIndex = mIndex - 1 + index;

        if (timedIndex < 0)
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

        if (mIndex == mBuffer.size()) {
            System.out.println("index = size");
            return mBuffer.get(mBuffer.size());
        }

        E tmp = mBuffer.get(mIndex);

        // set the element
        mBuffer.set(mIndex++, element);

        // wrap around the buffer positions
        if (mIndex >= mBuffer.size())
            mIndex = 0;

        return tmp;
    }

    /**
     * Get the number of elements in the ring buffer that were set by the user
     *
     * @return number of elements in the ring buffer that were set by the user
     */
    public int size() {
        return mSize;
    }
}
