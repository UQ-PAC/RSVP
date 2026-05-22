package uq.pac.childrenclinic.system;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "user_role_levels")
public class UserRoleLevel {

	@EmbeddedId
	private UserRoleLevelId id = new UserRoleLevelId();

	@ManyToOne(fetch = FetchType.EAGER)
	@MapsId("userId")
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne(fetch = FetchType.EAGER)
	@MapsId("roleId")
	@JoinColumn(name = "role_id")
	private Role role;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "level_id", nullable = false)
	private Level level;

	public UserRoleLevel() {
	}

	public UserRoleLevel(User user, Role role, Level level) {
		this.user = user;
		this.role = role;
		this.level = level;
		this.id = new UserRoleLevelId(user.getId(), role.getId());
	}

	public UserRoleLevelId getId() {
		return id;
	}

	public void setId(UserRoleLevelId id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
		if (user != null && this.id != null) {
			this.id = new UserRoleLevelId(user.getId(), this.id.getRoleId());
		}
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
		if (role != null) {
			this.id.setRoleId(role.getId());
		}
	}

	public Level getLevel() {
		return level;
	}

	public void setLevel(Level level) {
		this.level = level;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		UserRoleLevel that = (UserRoleLevel) o;
		return id != null && id.getUserId() != null && id.getRoleId() != null && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

}
