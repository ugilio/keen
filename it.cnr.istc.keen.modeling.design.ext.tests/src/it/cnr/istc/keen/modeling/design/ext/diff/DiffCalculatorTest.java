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

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import it.cnr.istc.keen.modeling.design.ext.diff.DiffCalculator.Chunk;
import it.cnr.istc.keen.modeling.design.ext.diff.DiffCalculator.Differences;
import it.cnr.istc.keen.modeling.design.ext.diff.DiffCalculator.Operation;

public class DiffCalculatorTest {
	DiffCalculator<Character> fixture;
	
	private static List<Character> l(String str) {
		char c[] = str.toCharArray();
		List<Character> result = new ArrayList<>(c.length);
		for (int i = 0; i < c.length; i++)
			result.add(c[i]);
		return result;
	}

	private static <T> boolean eq(List<Operation<T>> list, Integer ...args) {
		if (list.size()!=args.length)
			return false;
		for (int i = 0; i < args.length; i++)
			if (list.get(i).position!=args[i])
				return false;
		return true;
	}
	
	@Before
	public void setUp() throws Exception {
		fixture = new DiffCalculator<>();
	}

	@Test
	public void testSub1() {
		List<Chunk> c = fixture.findSubsequences(l(""), l("abcdef"));
		assertThat(c.size(), is(0));
		assertThat(c.size(), is(0));
	}

	@Test
	public void testSub2() {
		List<Chunk> c = fixture.findSubsequences(l("bcde"), l(""));
		assertThat(c.size(), is(0));
	}

	@Test
	public void testSub3() {
		List<Chunk> c = fixture.findSubsequences(l("bcde"), l("fgh"));
		assertThat(c.size(), is(0));
	}
	
	@Test
	public void testSub4() {
		List<Chunk> c = fixture.findSubsequences(l("bcde"), l("abcdef"));
		assertThat(c.size(), is(1));
		assertThat(c.get(0).leftIdx, is(0));
		assertThat(c.get(0).rightIdx, is(1));
		assertThat(c.get(0).len, is(4));
	}
	
	@Test
	public void testSub5() {
		List<Chunk> c = fixture.findSubsequences(l("abcde"), l("abcdef"));
		assertThat(c.size(), is(1));
		assertThat(c.get(0).leftIdx, is(0));
		assertThat(c.get(0).rightIdx, is(0));
		assertThat(c.get(0).len, is(5));
	}

	@Test
	public void testSub6() {
		List<Chunk> c = fixture.findSubsequences(l("bcdef"), l("abcdef"));
		assertThat(c.size(), is(1));
		assertThat(c.get(0).leftIdx, is(0));
		assertThat(c.get(0).rightIdx, is(1));
		assertThat(c.get(0).len, is(5));
	}
	
	@Test
	public void testSub7() {
		List<Chunk> c = fixture.findSubsequences(l("c"), l("abcdef"));
		assertThat(c.size(), is(1));
		assertThat(c.get(0).leftIdx, is(0));
		assertThat(c.get(0).rightIdx, is(2));
		assertThat(c.get(0).len, is(1));
	}
	
	@Test
	public void testSub8() {
		List<Chunk> c = fixture.findSubsequences(l("c"), l("abcabc"));
		assertThat(c.size(), is(1));
		assertThat(c.get(0).leftIdx, is(0));
		assertThat(c.get(0).rightIdx, is(2));
		assertThat(c.get(0).len, is(1));
	}
	
	@Test
	public void testSub9() {
		List<Chunk> c = fixture.findSubsequences(l("abcdef"), l("abdef"));
		assertThat(c.size(), is(2));
		assertThat(c.get(0).leftIdx, is(0));
		assertThat(c.get(0).rightIdx, is(0));
		assertThat(c.get(0).len, is(2));
		assertThat(c.get(1).leftIdx, is(3));
		assertThat(c.get(1).rightIdx, is(2));
		assertThat(c.get(1).len, is(3));
	}
	
	@Test
	public void testDiff1() {
		Differences<Character> d = fixture.computeDifferences(l(""), l("abcdef"));
		assertThat(d.toDelete.size(), is(0));
		assertThat(d.toAdd.size(), is(6));
		assertThat(eq(d.toAdd,0,1,2,3,4,5),is(true));
	}

	@Test
	public void testDiff2() {
		Differences<Character> d = fixture.computeDifferences(l("bcde"), l(""));
		assertThat(d.toDelete.size(), is(4));
		assertThat(d.toAdd.size(), is(0));
		assertThat(eq(d.toDelete,0,1,2,3),is(true));
	}

	@Test
	public void testDiff3() {
		Differences<Character> d = fixture.computeDifferences(l("bcde"), l("fgh"));
		assertThat(d.toDelete.size(), is(4));
		assertThat(d.toAdd.size(), is(3));
		assertThat(eq(d.toDelete,0,1,2,3),is(true));
		assertThat(eq(d.toAdd,0,1,2),is(true));
	}
	
	@Test
	public void testDiff4() {
		Differences<Character> d = fixture.computeDifferences(l("bcde"), l("abcdef"));
		assertThat(d.toDelete.size(), is(0));
		assertThat(d.toAdd.size(), is(2));
		assertThat(eq(d.toAdd,0,5),is(true));
	}
	
	@Test
	public void testDiff5() {
		Differences<Character> d = fixture.computeDifferences(l("abcde"), l("abcdef"));
		assertThat(d.toDelete.size(), is(0));
		assertThat(d.toAdd.size(), is(1));
		assertThat(eq(d.toAdd,5),is(true));
	}

	@Test
	public void testDiff6() {
		Differences<Character> d = fixture.computeDifferences(l("bcdef"), l("abcdef"));
		assertThat(d.toDelete.size(), is(0));
		assertThat(d.toAdd.size(), is(1));
		assertThat(eq(d.toAdd,0),is(true));
	}
	
	@Test
	public void testDiff7() {
		Differences<Character> d = fixture.computeDifferences(l("c"), l("abcdef"));
		assertThat(d.toDelete.size(), is(0));
		assertThat(d.toAdd.size(), is(5));
		assertThat(eq(d.toAdd,0,1,3,4,5),is(true));
	}
	
	@Test
	public void testDiff8() {
		Differences<Character> d = fixture.computeDifferences(l("c"), l("abcabc"));
		assertThat(d.toDelete.size(), is(0));
		assertThat(d.toAdd.size(), is(5));
		assertThat(eq(d.toAdd,0,1,3,4,5),is(true));
	}
	
	@Test
	public void testDiff9() {
		Differences<Character> d = fixture.computeDifferences(l("abcdef"), l("abdef"));
		assertThat(d.toDelete.size(), is(1));
		assertThat(d.toAdd.size(), is(0));
		assertThat(eq(d.toDelete,2),is(true));
	}	

}
