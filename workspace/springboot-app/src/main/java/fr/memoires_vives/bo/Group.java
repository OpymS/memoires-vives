package fr.memoires_vives.bo;

import java.util.List;

public class Group {
	private int groupId;
	private String name;
	private User owner;
	private List<User> members;

	public Group() {
	}

	/**
	 * @return the groupId
	 */
	public int getGroupId() {
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
	public void setGroupId(int groupId) {
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
