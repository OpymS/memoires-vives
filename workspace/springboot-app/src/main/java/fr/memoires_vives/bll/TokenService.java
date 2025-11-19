package fr.memoires_vives.bll;

import fr.memoires_vives.bo.User;

public interface TokenService {
	public void deleteToken(String token);
	public String createTokenForUser(User user, int minutes);
}
