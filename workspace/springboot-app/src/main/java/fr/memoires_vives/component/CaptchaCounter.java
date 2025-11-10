package fr.memoires_vives.component;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

@Component
public class CaptchaCounter {
	private final AtomicInteger count = new AtomicInteger(0);

	public int increment() {
		return count.incrementAndGet();
	}
	
	public int getCount() {
		return count.get();
	}

}
