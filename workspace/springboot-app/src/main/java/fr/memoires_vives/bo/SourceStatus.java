package fr.memoires_vives.bo;

public enum SourceStatus {
	PENDING("À valider"),
	APPROVED("Acceptées"),
	REJECTED("Refusées");
	
	private final String label;
	
	SourceStatus(String label){
		this.label = label;
	}
	
	public String getLabel() {
		return label;
	}
}
