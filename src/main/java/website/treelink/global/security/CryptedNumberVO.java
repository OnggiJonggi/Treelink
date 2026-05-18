package website.treelink.global.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 회원 식별번호 등 서버 내부에서 사용하는 식별번호를
 * 뷰 페이지에 보낼 때 암호화해서 사용하는데
 * 암호화할 때 내보내는 로직
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CryptedNumberVO {
	private int number;
	private String encryptedNumber;
}
