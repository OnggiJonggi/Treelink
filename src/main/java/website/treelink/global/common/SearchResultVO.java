package website.treelink.global.common;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class SearchResultVO<T> {
	private List<T> list;
	private int totalCount;
}
