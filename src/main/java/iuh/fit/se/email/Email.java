package iuh.fit.se.email;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Email {
	@NotBlank(message = "Email cannot be blank")
	private String toEmail;
	@NotBlank(message = "Subject cannot be blank")
	private String subject;
	@NotBlank(message = "Body cannot be blank")
	private String body;
}
