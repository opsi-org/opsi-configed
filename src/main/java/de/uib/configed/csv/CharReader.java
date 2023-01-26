package de.uib.configed.csv;

public class CharReader {
	private char[] buff;
	private int size = 0;
	private int index = 0;
	private int lastRead = 0;

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
		char item = buff[index++];
		index %= size;
		return item;
	}

	public char lastRead() {
		return buff[(lastRead + size - 1) % size];
	}

	public boolean isLastElement() {
		return index == size - 1;
	}

	public int size() {
		return size;
	}
}
