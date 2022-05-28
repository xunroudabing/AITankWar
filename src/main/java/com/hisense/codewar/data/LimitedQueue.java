package com.hisense.codewar.data;

import java.util.concurrent.LinkedBlockingQueue;

class LimitedQueue<E> extends LinkedBlockingQueue<E> {
	private static final long serialVersionUID = 1L;
	private final int size;

	public LimitedQueue(int size) {
		this.size = size;
	}

	@Override
	public boolean add(E o) {
		super.add(o);
		while (size() > size) {
			super.remove();
		}
		return true;
	}
}
