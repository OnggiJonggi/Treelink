package website.treelink.global.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
	
	/**
	 * 메인 화면으로 끄져라 하는 메쏘드에요!
	 * @return 메인 화면 경로
	 */
	@GetMapping("/")
	public String main() {
		return "common/main";
	}
}
