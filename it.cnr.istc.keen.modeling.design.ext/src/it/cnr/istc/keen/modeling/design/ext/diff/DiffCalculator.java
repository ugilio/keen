/*
 * Copyright (c) 2016-2017 PST (http://istc.cnr.it/group/pst).
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Giulio Bernardi
 */
package it.cnr.istc.keen.modeling.design.ext.diff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class DiffCalculator<T> {

	private HashMap<Chunk, List<Chunk>> cache = new HashMap<>();
	
	public static abstract class Operation<T>
	{
		public T element;
		public int position;
		public Operation(T element, int position) {
			this.element = element;
			this.position = position;
		}
		
		public abstract Operation<T> apply(List<T> list);
	}
	
	public static class Insert<T> extends Operation<T>
	{
		public Insert(T element, int position) {
			super(element, position);
		}
		
		public Operation<T> apply(List<T> list) {
			list.add(position, element);
			return this;
		}
	}
	
	public static class Delete<T> extends Operation<T>
	{
		public Delete(T element, int position) {
			super(element, position);
		}
		
		public Operation<T> apply(List<T> list) {
			list.remove(position);
			return this;
		}
	}
	
	public static class Differences<T> {
		public List<Operation<T>> toDelete;
		public List<Operation<T>> toAdd;
		
		public Differences(List<Operation<T>> toDelete, List<Operation<T>> toAdd) {
			this.toDelete = toDelete;
			this.toAdd = toAdd;
		}
	}
	
	private boolean eq(T t1, T t2) {
		if (t1 == null || t2 == null)
			return t1 == t2;
		return t1.equals(t2);
	}
	
	private int findSubsequence(List<T> before, List<T> after, int x, int y, int len) {
		if (x < 0 || x>=before.size() || x+len>before.size() || y < 0 || y>=after.size())
			return -1;
		for (int i = y; i+len <= after.size(); i++) {
			boolean ok = true;
			for (int j = 0; j < len; j++)
				if (!eq(before.get(x+j),after.get(i+j))) {
					ok = false;
					break;
				}
			if (ok)
				return i;
		}
		return -1;
	}
	
	protected static class Chunk {
		int leftIdx;
		int rightIdx;
		int len;
		public Chunk(int left, int right, int len) {
			this.leftIdx = left;
			this.rightIdx = right;
			this.len = len;
		}
		
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Chunk))
				return false;
			Chunk that = (Chunk)o;
			return
					this.leftIdx == that.leftIdx &&
					this.rightIdx == that.rightIdx &&
					this.len == that.len;
		}
		
		@Override
		public int hashCode() {
			return toString().hashCode();
		}
		
		@Override
		public String toString() {
			return String.format("[%d, %d, %d]", leftIdx, rightIdx, len);
		}
	}
	
	private Chunk findSubsequenceOfSize(List<T> before, List<T> after, int x, int y, int len) {
		while (x+len<=before.size() && y+len<=after.size()) {
			int idx = findSubsequence(before, after, x, y, len);
			if (idx>=0)
				return new Chunk(x,idx,len);
			x++;
		}
		return null;
	}
	
	
	private int getTotalLenght(List<Chunk> list) {
		int tot = 0;
		if (list == null)
			return tot;
		for (Chunk c : list)
			tot+=c.len;
		return tot;
	}
	
	private List<Chunk> findSubsequencesAfter(List<T> before, List<T> after, int x, int y) {
		List<Chunk> tmp = cache.get(new Chunk(x,y,0));
		if (tmp!=null)
			return tmp;
		int size = Math.min(before.size()-x,after.size()-y);
		int bestScore = 0;
		List<Chunk> bestSequence = Collections.emptyList();
		int i = x;
		int j = y;
		while(size>0) {
			Chunk c = findSubsequenceOfSize(before, after, i, j, size);
			if (c!=null) {
				List<Chunk> list = new ArrayList<>(size);
				list.add(c);
				list.addAll(findSubsequencesAfter(before, after, c.leftIdx+size, c.rightIdx+size));
				int score = getTotalLenght(list);
				if (score>bestScore) {
					bestScore = score;
					bestSequence = list;
				}
				i+=size;
				j+=size;
			}
			else {
				i = x;
				j = y;
				size--;
			}
		}
		cache.put(new Chunk(x, y, 0), bestSequence);
		return bestSequence;
	}
	
	protected List<Chunk> findSubsequences(List<T> before, List<T> after) {
		return findSubsequencesAfter(before, after, 0, 0);
	}
	
	
    public Differences<T> computeDifferences(List<T> before, List<T> after) {
    	List<Chunk> sequence = findSubsequences(before, after);
    	int x = 0;
    	int y = 0;
    	//elements to delete, in source list "coordinates"
    	List<Operation<T>> del = new ArrayList<>(before.size());
    	//elements to insert, in destination list "coordinates"
    	List<Operation<T>> ins = new ArrayList<>(before.size());
    	//add artificial chunk to signal the end
    	sequence = new ArrayList<>(sequence);
    	sequence.add(new Chunk(before.size(),after.size(),0));
    	for (Chunk c : sequence) {
    		for (; x < c.leftIdx; x++)
    			del.add(new Delete<T>(before.get(x), x));
    		for (; y < c.rightIdx; y++)
    			ins.add(new Insert<T>(after.get(y), y));
    		x+=c.len;
    		y+=c.len;
    	}
    	cache.clear();
    	return new Differences<T>(del,ins);
    }

}
