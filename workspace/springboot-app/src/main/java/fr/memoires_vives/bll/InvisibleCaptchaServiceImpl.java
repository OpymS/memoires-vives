package fr.memoires_vives.bll;

import org.springframework.stereotype.Service;

import fr.memoires_vives.component.CaptchaCounter;

@Service
public class InvisibleCaptchaServiceImpl implements InvisibleCaptchaService {
	private final CaptchaCounter counter;

	private static final long MIN_DELAY_MS = 3000;

	public InvisibleCaptchaServiceImpl(CaptchaCounter counter) {
		this.counter = counter;
	}

	@Override
	public boolean isBot(String website, long formTimestamp) {
		long now = System.currentTimeMillis();

		boolean honeypotTriggered = website != null && !website.trim().isEmpty();
		boolean tooFast = (now - formTimestamp) < MIN_DELAY_MS;

		if (honeypotTriggered || tooFast) {
			counter.increment();
			return true;
		}
		return false;
	}

}
