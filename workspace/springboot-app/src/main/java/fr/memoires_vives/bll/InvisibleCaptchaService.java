package fr.memoires_vives.bll;

public interface InvisibleCaptchaService {
	public boolean isBot(String website, long formTimestamp);
}
