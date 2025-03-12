package iuh.fit.se.entities;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Embeddable
public class ModifyProductId implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	@JoinColumn(name = "modified_by")
	private Long modifiedBy;
	

	@JoinColumn(name = "glass_id")
	private Long glass;

	public ModifyProductId(Long modifiedBy, Long glass) {
		super();
		this.modifiedBy = modifiedBy;
		this.glass = glass;
	}

	public ModifyProductId() {
		super();
	}

	@Override
	public int hashCode() {
		return Objects.hash(modifiedBy, glass);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ModifyProductId))
			return false;
		ModifyProductId other = (ModifyProductId) obj;
		return Objects.equals(modifiedBy, other.modifiedBy) && Objects.equals(glass, other.glass);
	}

	public Long getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(Long modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public Long getGlass() {
		return glass;
	}

	public void setGlass(Long glass) {
		this.glass = glass;
	}
	
	
}
