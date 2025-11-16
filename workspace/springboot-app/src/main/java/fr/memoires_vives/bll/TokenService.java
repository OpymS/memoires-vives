package fr.memoires_vives.bll;

import fr.memoires_vives.bo.User;

public interface TokenService {
	public void deleteToken(String token);
	void createTokenForUser(User user, String token, int minutes);
}
