package iuh.fit.se.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Role {
	ADMIN, USER, SUPER;
	
	@JsonValue
    public String getRole() {
        return name();
    }
}
