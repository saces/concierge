/**
 * 
 */
package com.buglabs.osgi.http.pub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * A ADT for storing an arbitrary list of bytes.  Structure will internally allocate memory as bytes are added.
 * @author ken
 *
 */
public class DynamicByteBuffer {
	private final List segments;

	private Segment currentSegment = null;

	private int totalBytes = 0;
	
	private final int segmentSize;

	public DynamicByteBuffer() {
		segmentSize = -1;
		segments = new ArrayList();
	}
	
	public DynamicByteBuffer(int segmentSize) {
		this.segmentSize = segmentSize;
		segments = new ArrayList();
	}

	public void append(byte b) {
		if (currentSegment == null || currentSegment.isFull()) {
			segments.add(createSegment());

			currentSegment = (Segment) segments.get(segments.size() - 1);
		}

		currentSegment.append(b);

		totalBytes++;
	}

	private Segment createSegment() {
		if (segmentSize > -1) {
			return new Segment(segmentSize);
		}
		
		return new Segment();
	}

	/**
	 * Return byte array of all bytes that have been appended in order.
	 * @return
	 */
	public byte[] toArray() {
		byte[] a = new byte[totalBytes];
		int pi = 0;

		for (Iterator i = segments.iterator(); i.hasNext();) {
			Segment s = (Segment) i.next();

			for (int j = 0; j < s.getSize(); ++j) {
				a[pi] = s.get(j);
				pi++;
			}
		}

		return a;
	}

	/**
	 * Represents one allocation block.
	 * @author ken
	 *
	 */
	private class Segment {
		/**
		 * Default size of segment if no size specified in constructor.
		 */
		private static final int DEFAULT_SIZE = 256;

		byte[] mem;
		int size;
		int index = 0;

		/**
		 * Allocate segment.  Uses DEFAULT_SIZE.
		 *
		 */
		public Segment() {
			size = DEFAULT_SIZE;
			mem = new byte[size];
			Arrays.fill(mem, (byte) 0);
		}
		
		/**
		 * Allocate segment.
		 * @param size number of bytes to allocate.
		 */
		public Segment(int size) {
			this.size = size;
			mem = new byte[size];
			Arrays.fill(mem, (byte) 0);
		}

		public void append(byte b) {
			mem[index] = b;
			index++;
		}

		public boolean isFull() {
			return index == (size - 1);
		}

		public int getSize() {
			return index;
		}

		public byte get(int index) {
			return mem[index];
		}
	}
}