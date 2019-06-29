package com.github.f4b6a3.uuid.util;

import org.junit.Test;

import static org.junit.Assert.*;

import java.time.Instant;

public class TimestampUtilTest {

	@Test
	public void testToInstantFromInstant() {
		Instant instant1 = Instant.now();
		long timestamp1 = TimestampUtil.toTimestamp(instant1);
		Instant instant2 = TimestampUtil.toInstant(timestamp1);
		assertEquals(instant1, instant2);
	}

	@Test
	public void testFromUnixEpochMillisecondsToTimestamp() {
		long milliseconds = System.currentTimeMillis();
		long timestamp = TimestampUtil.toTimestamp(milliseconds);
		Instant instant = TimestampUtil.toInstant(timestamp);
		assertEquals(milliseconds, instant.toEpochMilli());
	}
	
	@Test
	public void testFromCurrentTimestampToUnixEpochMilliseconds() {
		long timestamp = TimestampUtil.getCurrentTimestamp();
		long milliseconds = TimestampUtil.toUnixEpochMilliseconds(timestamp);
		Instant instant = TimestampUtil.toInstant(timestamp);
		assertEquals(milliseconds, instant.toEpochMilli());
	}
	
	@Test
	public void testFromTimestampToUnixEpochMilliseconds() {
		long milliseconds1 = System.currentTimeMillis();
		long timestamp = TimestampUtil.toTimestamp(milliseconds1);
		long milliseconds2 = TimestampUtil.toUnixEpochMilliseconds(timestamp);
		assertEquals(milliseconds1, milliseconds2);
	}
}
