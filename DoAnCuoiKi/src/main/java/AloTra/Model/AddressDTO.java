package AloTra.Model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {
    private Long id;
    private Long accountId;
    private String accountUsername;
    private String fullName;
    private String phone;
    private String street;
    private String wardCode;
    private String districtCode;
    private String provinceCode;
    private String fullAddressText;
    private Boolean isDefault;
}