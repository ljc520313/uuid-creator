/*
 * MIT License
 * 
 * Copyright (c) 2020 Fabio Lima
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.f4b6a3.uuid.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

import static com.github.f4b6a3.uuid.util.UuidUtil.applyVersion;
import static com.github.f4b6a3.uuid.util.internal.ByteUtil.toHexadecimal;
import static com.github.f4b6a3.uuid.util.internal.ByteUtil.toNumber;

/**
 * Utility class to generate machine ID.
 * 
 * It works in three steps:
 * 
 * 1. Create a string containing host name, MAC and IP;
 * 
 * 2. Create a hash of the string using SHA-256 algorithm;
 * 
 * 3. Create the identifier using part of the resulting hash.
 * 
 * If the host name, MAC or IP changes, the identifier will also change.
 */
public final class MachineId {

	private MachineId() {
	}

	/**
	 * Returns a number generated from the machine hash.
	 * 
	 * It uses the first 8 bytes of the machine hash.
	 * 
	 * @return a number
	 */
	public static long getMachineId() {
		final byte[] bytes = getMachineHash();
		return toNumber(bytes, 0, 8);
	}

	/**
	 * Returns a UUID generated from the machine hash.
	 * 
	 * It uses the first 16 bytes of the machine hash.
	 * 
	 * The UUID version is 4.
	 * 
	 * @return a UUID
	 */
	public static UUID getMachineUuid() {
		final byte[] bytes = getMachineHash();
		final long msb = toNumber(bytes, 0, 8);
		final long lsb = toNumber(bytes, 8, 16);
		final UUID uuid = new UUID(msb, lsb);
		return applyVersion(uuid, 4);
	}

	/**
	 * Returns the machine hash in hexadecimal format.
	 * 
	 * The returning string has 64 chars.
	 * 
	 * @return a string
	 */
	public static String getMachineHexa() {
		final byte[] bytes = getMachineHash();
		return toHexadecimal(bytes);
	}

	/**
	 * Returns the machine hash in a byte array.
	 * 
	 * @return a byte array
	 */
	public static byte[] getMachineHash() {
		final String string = getMachineString();
		return MessageDigestHolder.digest(string);
	}

	/**
	 * Returns a string containing host name, MAC and IP.
	 * 
	 * Output format: "hostname123 11-22-33-44-55-66 123.123.123.123"
	 * 
	 * Note: a network interface may have more than one IP address.
	 * 
	 * @return a string
	 */
	public static String getMachineString() {

		String hostName = null;
		String nicMac = null;
		String nicAddr = null;

		try {
			// Get the host name
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			// do nothing
		}

		try {
			// Get the MAC and IP addresses
			Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
			while (nics.hasMoreElements()) {
				NetworkInterface nic = nics.nextElement();
				if (nic != null && !nic.isLoopback()) {
					byte[] mac = nic.getHardwareAddress();
					Enumeration<InetAddress> ips = nic.getInetAddresses();
					if (mac != null && mac.length == 6 && ips.hasMoreElements()) {
						nicMac = formatMac(mac);
						List<String> list = new ArrayList<>();
						while (ips.hasMoreElements()) {
							InetAddress ip = ips.nextElement();
							list.add(ip.getHostAddress());
						}
						nicAddr = String.join(" ", list);
						break; // take only one
					}
				}
			}
		} catch (SocketException e) {
			// do nothing
		}

		return String.join(" ", hostName, nicMac, nicAddr);
	}

	/**
	 * Returns the string format of a MAC address
	 * 
	 * Output format: 00-00-00-00-00-00
	 * 
	 * @param mac MAC address bytes
	 * @return a string
	 */
	private static String formatMac(byte[] mac) {
		String[] hex = new String[mac.length];
		for (int i = 0; i < mac.length; i++) {
			hex[i] = String.format("%02X", mac[i]);
		}
		return String.join("-", hex);
	}

	/**
	 * Class that holds private instance of the message digest.
	 */
	private static class MessageDigestHolder {
		static final MessageDigest INSTANCE = getMessageDigest();

		static synchronized byte[] digest(String string) {
			return MessageDigestHolder.INSTANCE.digest(string.getBytes());
		}

		private static MessageDigest getMessageDigest() {
			try {
				return MessageDigest.getInstance("SHA-256");
			} catch (NoSuchAlgorithmException e) {
				throw new InternalError("Message digest algorithm not supported.", e);
			}
		}
	}
}
