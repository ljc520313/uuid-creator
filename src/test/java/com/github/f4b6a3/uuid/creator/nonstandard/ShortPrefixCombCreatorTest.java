package com.github.f4b6a3.uuid.creator.nonstandard;

import org.junit.Test;

import com.github.f4b6a3.uuid.UuidCreator;
import com.github.f4b6a3.uuid.creator.AbstractUuidCreatorTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

public class ShortPrefixCombCreatorTest extends AbstractUuidCreatorTest {

	private static final long ONE_MINUTE = 60_000;
	
	@Test
	public void testCompGuid() {

		UUID[] list = new UUID[DEFAULT_LOOP_MAX];
		long startTime = (System.currentTimeMillis() / ONE_MINUTE) & 0x000000000000ffffL;

		for (int i = 0; i < DEFAULT_LOOP_MAX; i++) {
			list[i] = UuidCreator.getShortPrefixComb();
		}

		long endTime = (System.currentTimeMillis() / ONE_MINUTE) & 0x000000000000ffffL;

		checkOrdering(list);
		checkUniqueness(list);

		long previous = 0;
		for (int i = 0; i < DEFAULT_LOOP_MAX; i++) {
			long creationTime = list[i].getMostSignificantBits() >>> 48;
			assertTrue("Comb Guid creation time before start time", startTime <= creationTime);
			assertTrue("Comb Guid creation time after end time", creationTime <= endTime);
			assertTrue("Comb Guid sequence is not sorted " + previous + " " + creationTime,
					previous <= creationTime);
			previous = creationTime;
		}
	}

	@Test
	public void testCombGuid() {

		UUID[] list = new UUID[DEFAULT_LOOP_MAX];
		ShortPrefixCombCreator creator = UuidCreator.getShortPrefixCombCreator();

		for (int i = 0; i < DEFAULT_LOOP_MAX; i++) {
			list[i] = creator.create();
			assertNotNull("UUID is null", list[i]);
		}

		checkOrdering(list);
		checkUniqueness(list);
	}

	@Test
	public void testCombGuidWithCustomRandomGenerator() {

		UUID[] list = new UUID[DEFAULT_LOOP_MAX];
		Random random = new Random();
		ShortPrefixCombCreator creator = UuidCreator.getShortPrefixCombCreator().withRandomGenerator(random);
		;

		for (int i = 0; i < DEFAULT_LOOP_MAX; i++) {
			list[i] = creator.create();
			assertNotNull("UUID is null", list[i]);
		}

		checkOrdering(list);
		checkUniqueness(list);
	}

	@Test
	public void testCombGuidWithCustomRandomGeneratorSecure() {

		UUID[] list = new UUID[DEFAULT_LOOP_MAX];
		Random random = new SecureRandom();
		ShortPrefixCombCreator creator = UuidCreator.getShortPrefixCombCreator().withRandomGenerator(random);

		for (int i = 0; i < DEFAULT_LOOP_MAX; i++) {
			list[i] = creator.create();
			assertNotNull("UUID is null", list[i]);
		}

		checkOrdering(list);
		checkUniqueness(list);
	}

	@Override
	protected void checkOrdering(UUID[] list) {
		UUID[] other = Arrays.copyOf(list, list.length);
		Arrays.sort(other);

		for (int i = 0; i < list.length; i++) {
			long x = list[i].getMostSignificantBits() >>> 48;
			long y = other[i].getMostSignificantBits() >>> 48;
			assertEquals("The UUID list is not ordered", x, y);
		}
	}
	
	@Test
	public void testGetShortPrefixCombParallelGeneratorsShouldCreateUniqueUuids() throws InterruptedException {

		Thread[] threads = new Thread[THREAD_TOTAL];
		TestThread.clearHashSet();

		// Instantiate and start many threads
		for (int i = 0; i < THREAD_TOTAL; i++) {
			threads[i] = new TestThread(UuidCreator.getShortPrefixCombCreator(), DEFAULT_LOOP_MAX);
			threads[i].start();
		}

		// Wait all the threads to finish
		for (Thread thread : threads) {
			thread.join();
		}

		// Check if the quantity of unique UUIDs is correct
		assertEquals(DUPLICATE_UUID_MSG, TestThread.hashSet.size(), (DEFAULT_LOOP_MAX * THREAD_TOTAL));
	}
}
