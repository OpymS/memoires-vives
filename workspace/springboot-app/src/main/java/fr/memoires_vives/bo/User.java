package fr.memoires_vives.bo;

import java.util.List;

public class User {
	private int userId;
	private String pseudo;
	private String email;
	private String password;
	private List<Memory> memories;
	private boolean admin;
	private boolean activated;
	private List<Group> groups;

	public User() {
	}

	public User(int userId, String pseudo, String email, String password, List<Memory> memories, boolean admin,
			boolean activated) {
		this.userId = userId;
		this.pseudo = pseudo;
		this.email = email;
		this.password = password;
		this.memories = memories;
		this.admin = admin;
		this.activated = activated;
	}

	/**
	 * @return the userId
	 */
	public int getUserId() {
		return userId;
	}

	/**
	 * @return the pseudo
	 */
	public String getPseudo() {
		return pseudo;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @return the memories
	 */
	public List<Memory> getMemories() {
		return memories;
	}

	/**
	 * @return the admin
	 */
	public boolean isAdmin() {
		return admin;
	}

	/**
	 * @return the activated
	 */
	public boolean isActivated() {
		return activated;
	}

	/**
	 * @return the groups
	 */
	public List<Group> getGroups() {
		return groups;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(int userId) {
		this.userId = userId;
	}

	/**
	 * @param pseudo the pseudo to set
	 */
	public void setPseudo(String pseudo) {
		this.pseudo = pseudo;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @param memories the memories to set
	 */
	public void setMemories(List<Memory> memories) {
		this.memories = memories;
	}

	/**
	 * @param admin the admin to set
	 */
	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	/**
	 * @param activated the activated to set
	 */
	public void setActivated(boolean activated) {
		this.activated = activated;
	}

	/**
	 * @param groups the groups to set
	 */
	public void setGroups(List<Group> groups) {
		this.groups = groups;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("User [userId=");
		builder.append(userId);
		builder.append(", pseudo=");
		builder.append(pseudo);
		builder.append(", email=");
		builder.append(email);
		builder.append(", password=");
		builder.append(password);
		builder.append(", memories=");
		builder.append(memories);
		builder.append(", admin=");
		builder.append(admin);
		builder.append(", activated=");
		builder.append(activated);
		builder.append(", groups=");
		builder.append(groups);
		builder.append("]");
		return builder.toString();
	}
}
