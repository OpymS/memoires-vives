package fr.memoires_vives.dto;

import fr.memoires_vives.bo.SracnContactSubject;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SracnContactForm {
	@NotBlank(message="Veuillez saisir votre nom.")
	private String nom;

	@NotBlank(message="Veuillez saisir votre prénom.")
	private String prenom;

	@NotBlank(message="Veuillez saisir votre adresse email.")
	@Email(message="Veuillez saisir une adresse email valide.")
	private String email;

	private String telephone;

	@NotNull
	private SracnContactSubject subject;

	@NotBlank(message="Veuillez saisir un message.")
	private String message;

	/**
	 * @return the nom
	 */
	public String getNom() {
		return nom;
	}

	/**
	 * @return the prenom
	 */
	public String getPrenom() {
		return prenom;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @return the telephone
	 */
	public String getTelephone() {
		return telephone;
	}

	/**
	 * @return the subject
	 */
	public SracnContactSubject getSubject() {
		return subject;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param nom the nom to set
	 */
	public void setNom(String nom) {
		this.nom = nom;
	}

	/**
	 * @param prenom the prenom to set
	 */
	public void setPrenom(String prenom) {
		this.prenom = prenom;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @param telephone the telephone to set
	 */
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	/**
	 * @param subject the subject to set
	 */
	public void setSubject(SracnContactSubject subject) {
		this.subject = subject;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

}
