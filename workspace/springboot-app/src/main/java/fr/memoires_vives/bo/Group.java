package fr.memoires_vives.bo;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ugroups")
public class Group {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long groupId;

	@Column(nullable = false)
	private String name;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User owner;

	@ManyToMany(mappedBy = "groups")
	private List<User> members;

	public Group() {
	}

	/**
	 * @return the groupId
	 */
	public long getGroupId() {
		return groupId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the owner
	 */
	public User getOwner() {
		return owner;
	}

	/**
	 * @return the members
	 */
	public List<User> getMembers() {
		return members;
	}

	/**
	 * @param groupId the groupId to set
	 */
	public void setGroupId(long groupId) {
		this.groupId = groupId;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param owner the owner to set
	 */
	public void setOwner(User owner) {
		this.owner = owner;
	}

	/**
	 * @param members the members to set
	 */
	public void setMembers(List<User> members) {
		this.members = members;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Group [groupId=");
		builder.append(groupId);
		builder.append(", name=");
		builder.append(name);
		builder.append(", owner=");
		builder.append(owner);
		builder.append(", members=");
		builder.append(members);
		builder.append("]");
		return builder.toString();
	}
}
