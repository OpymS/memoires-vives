package fr.memoires_vives.bo;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long userId;

	@Column(nullable = false, unique = true)
	private String pseudo;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String password;
	
	@Column(nullable = false)
	private String role;

	@OneToMany(mappedBy = "rememberer", cascade = CascadeType.ALL)
	private List<Memory> memories;

	private boolean admin;
	private boolean activated;

	@ManyToMany
	@JoinTable(
		name = "group_user",
		joinColumns = @JoinColumn(name = "user_id"), 
		inverseJoinColumns = @JoinColumn(name = "group_id")
	)
	private List<Group> groups;
	
	@ManyToMany
    @JoinTable(
        name = "user_friends",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "friend_id")
    )
	private List<User> friends = new ArrayList<User>();


	public User() {
	}

	public User(String pseudo, String email, String password, List<Memory> memories, boolean admin, boolean activated) {
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
	public long getUserId() {
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
	 * @return the role
	 */
	public String getRole() {
		return role;
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
	 * @return the friends
	 */
	public List<User> getFriends() {
		return friends;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(long userId) {
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
	 * @param role the role to set
	 */
	public void setRole(String role) {
		this.role = role;
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

	/**
	 * @param friends the friends to set
	 */
	public void setFriends(List<User> friends) {
		this.friends = friends;
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
		builder.append(", role=");
		builder.append(role);
		builder.append(", memories=");
		builder.append(memories);
		builder.append(", admin=");
		builder.append(admin);
		builder.append(", activated=");
		builder.append(activated);
		builder.append(", groups=");
		builder.append(groups);
		builder.append(", friends=");
		builder.append(friends);
		builder.append("]");
		return builder.toString();
	}

}
