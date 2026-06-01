package fr.memoires_vives.bo;

public enum SracnContactSubject {
	INSCRIPTION("Inscription"), 
	INFORMATION("Demande d'information"), 
	COMPETITION("Compétition"),
	PARTENARIAT("Partenariat");

	private final String label;

	SracnContactSubject(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
