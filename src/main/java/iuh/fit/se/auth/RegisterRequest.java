package iuh.fit.se.auth;

import java.sql.Date;

import iuh.fit.se.entities.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Tên đăng nhập không được chứa ký tự đặc biệt")
    private String username;

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(min = 3, max = 50, message = "Họ và tên phải từ 3 đến 50 ký tự")
    private String fullname;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.])[A-Za-z\\d@$!%*?&.]{8,}$",
             message = "Mật khẩu phải chứa ít nhất 1 ký tự in hoa, 1 ký tự số, 1 ký tự đặc biệt và có ít nhất 8 ký tự")
    private String password;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotNull(message = "Ngày sinh không được để trống")
    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private Date dob;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0[0-9]{9}$", message = "Số điện thoại phải bắt đầu bằng 0 và có đúng 10 chữ số")
    private String phone;

    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(min = 3, max = 100, message = "Địa chỉ phải từ 3 đến 100 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9\\s,.'-]+$", message = "Địa chỉ không hợp lệ")
    private String address;
	private boolean gender;
    private Role role;
}
