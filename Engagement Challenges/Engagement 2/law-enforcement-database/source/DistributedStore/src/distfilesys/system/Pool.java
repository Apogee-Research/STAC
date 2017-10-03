package distfilesys.system;

import java.util.concurrent.Semaphore;

import distfilesys.util.Queue;

public class Pool<E> {

	protected Queue<E> array;

	protected int rr;

	protected Thread threads[];

	protected Semaphore empty;

	public Pool() {
		this(10);
	}

	public Pool(int poolSize) {
		array = new Queue<E>();
		threads = new Thread[poolSize];
		empty = new Semaphore(poolSize);
		rr = 0;

		for (int i = 0; i < threads.length; i++) {
			threads[i] = new Thread(this);
			threads[i].start();
		}
	}

	public synchronized void put(E s) {
		try {
			empty.acquire();
			array.enqueue(s);
			synchronized (threads[rr]) {
				threads[rr].notify();
			}
			rr = (rr + 1) % threads.length;
		} catch (Exception e) {
			Logger.severe("Pool error.", e);
		}
	}

	public synchronized E get() {
		E value = array.dequeue();
		empty.release();
		return value;
	}
}
