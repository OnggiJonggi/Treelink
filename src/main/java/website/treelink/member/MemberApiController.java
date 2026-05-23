package website.treelink.member;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/member")
public class MemberApiController {
	@Autowired
	private MemberService service;
	
	/**
	 * 아이디 중복 확인
	 * @param member
	 * @return 정상 200
	 */
	@GetMapping("/check-id")
	public ResponseEntity<Void> checkId(String userId) {
		service.checkId(userId);
		return ResponseEntity.ok().build();
	}
	
	/**
	 * 닉네임 중복 확인
	 * @param nickname
	 * @return 정상 200, 이상해요 500
	 */
	@GetMapping("/check-nickname")
	public ResponseEntity<Void> checkNickName(String nickname) {
		service.checkNick(nickname);
		return ResponseEntity.ok().build();
	}
}
