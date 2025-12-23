package fr.memoires_vives.bll;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.memoires_vives.component.CaptchaCounter;

@ExtendWith(MockitoExtension.class)
public class InvisibleCaptchaServiceImplTest {
	@Mock
	private CaptchaCounter captchaCounter;

	private InvisibleCaptchaServiceImpl service;

	@Test
	void shouldReturnTrueAndIncrementCounter_whenHoneypotIsTriggered() {
		service = new InvisibleCaptchaServiceImpl(captchaCounter);

		String website = "https://bot.example";
		long formTimestamp = System.currentTimeMillis() - 3001;

		boolean result = service.isBot(website, formTimestamp);

		assertTrue(result);
		verify(captchaCounter).increment();
	}

	@Test
	void shouldReturnTrueAndIncrementCounter_whenSubmissionIsTooFast() {
		service = new InvisibleCaptchaServiceImpl(captchaCounter);

		String website = null;
		long formTimestamp = System.currentTimeMillis();

		boolean result = service.isBot(website, formTimestamp);

		assertTrue(result);
		verify(captchaCounter).increment();
	}

	@Test
	void shouldReturnFalseAndNotIncrementCounter_whenWebsiteIsBlankAndDelayIsRespected() {
		service = new InvisibleCaptchaServiceImpl(captchaCounter);

		String website = "   ";
		long formTimestamp = System.currentTimeMillis() - 3001;

		boolean result = service.isBot(website, formTimestamp);

		assertFalse(result);
		verify(captchaCounter, never()).increment();
	}

	@Test
	void shouldReturnFalseAndNotIncrementCounter_whenNoBotIndicators() {
		service = new InvisibleCaptchaServiceImpl(captchaCounter);

		String website = null;
		long formTimestamp = System.currentTimeMillis() - 3001;

		boolean result = service.isBot(website, formTimestamp);

		assertFalse(result);
		verify(captchaCounter, never()).increment();
	}

	@Test
	void shouldIncrementCounterOnlyOnce_whenBothHoneypotAndTooFastAreTriggered() {
		service = new InvisibleCaptchaServiceImpl(captchaCounter);

		String website = "filled";
		long formTimestamp = System.currentTimeMillis();

		boolean result = service.isBot(website, formTimestamp);

		assertTrue(result);
		verify(captchaCounter, times(1)).increment();
	}
}
