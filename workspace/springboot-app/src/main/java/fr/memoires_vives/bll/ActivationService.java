package fr.memoires_vives.bll;

import fr.memoires_vives.bo.User;

public interface ActivationService {

	public void requestActivation(User user);
	public User activateUser(String token);
	public void regenerateTokenAndSendMail(User user);
}
