package de.ovgu.softwareprojekt.pipeline;

import com.sun.istack.internal.Nullable;
import de.ovgu.softwareprojekt.networking.NetworkDataSink;
import de.ovgu.softwareprojekt.networking.NetworkDataSource;
import de.ovgu.softwareprojekt.pipeline.filters.AbstractFilter;

import java.util.LinkedList;
import java.util.List;

/**
 * This builder class can only handle {@link de.ovgu.softwareprojekt.pipeline.filters.AbstractFilter}
 * subclasses, for it shall have an unified interface to chain the elements.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class FilterPipelineBuilder {
    /**
     * This is where we store
     */
    private LinkedList<AbstractFilter> mPipelineElements = new LinkedList<>();

    /**
     * Insert a pipeline element as the first element
     *
     * @param sink the abstract filter that will be the first to receive data
     */
    public void prepend(AbstractFilter sink) {
        mPipelineElements.push(sink);
    }

    /**
     * Insert a pipeline element as the last element
     *
     * @param filter the abstract filter that will be the last to receive data
     */
    public void append(AbstractFilter filter) {
        mPipelineElements.add(filter);
    }

    /**
     * Insert an {@link AbstractFilter} pipeline element into an arbitrary position.
     * Shifts the element currently at that position (if any) and
     * any subsequent elements to the right (adds one to their indices).
     *
     * @param sink  the sink to be added
     * @param index where to put the
     * @throws IndexOutOfBoundsException - if the index is out of range {@literal (index < 0 || index > size())}
     */
    public void append(AbstractFilter sink, int index) throws IndexOutOfBoundsException {
        mPipelineElements.add(index, sink);
    }

    /**
     * Get the element at the supplied index.
     * You must not call {@link AbstractFilter#close()} without calling {@link #remove} first.
     *
     * @param index index of the element
     * @return the abstract filter that is at the given position
     */
    public AbstractFilter get(int index) {
        return mPipelineElements.get(index);
    }

    /**
     * Remove the element at the given position from the pipeline
     *
     * @param index index of the element that should no longer be part of the pipeline
     */
    public void remove(int index) {
        mPipelineElements.remove(index);
    }

    /**
     * Remove the given element from the pipeline to be built
     *
     * @param element element that should no longer be part of the pipeline
     */
    public void remove(AbstractFilter element) {
        mPipelineElements.remove(element);
    }

    /**
     * Chain the listed {@link AbstractFilter} pipeline elements
     *
     * @return head of the pipeline, or null if no elements were added
     */
    public NetworkDataSink build() {
        for (int i = 1; i < mPipelineElements.size(); i++)
            mPipelineElements.get(i - 1).setDataSink(mPipelineElements.get(i));
        return mPipelineElements.get(0);
    }

    /**
     * Chain the given pipeline elements, putting a {@link NetworkDataSink} as the last element
     *
     * @param lastElement the final element to receive data from this pipeline
     * @return head of the pipeline
     */
    public NetworkDataSink build(NetworkDataSink lastElement) {
        for (int i = 1; i < mPipelineElements.size(); i++)
            mPipelineElements.get(i - 1).setDataSink(mPipelineElements.get(i));
        mPipelineElements.getLast().setDataSink(lastElement);
        return mPipelineElements.get(0);
    }
}
