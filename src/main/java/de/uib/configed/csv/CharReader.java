/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.csv;

public class CharReader {
	private char[] buff;
	private int size;
	private int index;
	private int lastRead;

	private boolean peeked;
	private char peekedItem;

	public CharReader(char[] buff, int size) {
		this.size = size;
		this.buff = buff;
	}

	public char consume() {
		char item = peek();
		peeked = false;

		return item;
	}

	public char peek() {
		if (!peeked) {
			peekedItem = get();
		}

		peeked = true;
		return peekedItem;
	}

	private char get() {
		lastRead = index - 1;
		char item = buff[index];
		index++;
		index %= size;
		return item;
	}

	public char lastRead() {
		return buff[(lastRead + size - 1) % size];
	}

	public int size() {
		return size;
	}
}
