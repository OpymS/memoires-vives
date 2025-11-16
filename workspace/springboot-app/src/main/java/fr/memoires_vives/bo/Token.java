package fr.memoires_vives.bo;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="password_reset_token")
public class Token {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long tokenId;

	@Column(nullable = false, unique = true)
	private String token;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(nullable = false, name = "user_id")
	private User user;

	@Column(nullable = false)
	private LocalDateTime expiration;

	public Token() {
	}

	public Token(String token, User user, LocalDateTime expiration) {
		this.token = token;
		this.user = user;
		this.expiration = expiration;
	}

	/**
	 * @return the tokenId
	 */
	public long getTokenId() {
		return tokenId;
	}

	/**
	 * @return the token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @return the expiration
	 */
	public LocalDateTime getExpiration() {
		return expiration;
	}

	/**
	 * @param tokenId the tokenId to set
	 */
	public void setTokenId(long tokenId) {
		this.tokenId = tokenId;
	}

	/**
	 * @param token the token to set
	 */
	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * @param expiration the expiration to set
	 */
	public void setExpiration(LocalDateTime expiration) {
		this.expiration = expiration;
	}

}
