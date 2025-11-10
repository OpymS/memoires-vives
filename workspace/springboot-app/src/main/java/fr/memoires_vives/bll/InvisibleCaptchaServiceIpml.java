package fr.memoires_vives.bll;

import org.springframework.stereotype.Service;

@Service
public class InvisibleCaptchaServiceIpml implements InvisibleCaptchaService {

	private static final long MIN_DELAY_MS = 3000;

	@Override
	public boolean isBot(String website, long formTimestamp) {
		long now = System.currentTimeMillis();

		boolean honeypotTriggered = website != null && !website.trim().isEmpty();
		boolean tooFast = (now - formTimestamp) < MIN_DELAY_MS;

		return honeypotTriggered || tooFast;
	}

}
