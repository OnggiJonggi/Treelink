package com.tl.global.common;

/**
 * 검색 요청시 사용하는 VO
 * 이거 상속받아서 써라.
 */
public class SearchPageVO {
    private int page = 1;
    // int startRow;
    // int endRow;

    public int getPage() {
    	return page;
	}
    public void setPage(int page) {
    	// 1페이지 미만이면 1페이지로 바꿔줌
        this.page = (page < 1) ? 1 : page;
    }

    /**
     * startRow 검색 데이터 시작행
     * endRow 끝행
     * 계산 필드는 필드 없이 getter/setter만 작성해요
     */
    public int getStartRow() {
        return (page - 1) * PagingConstant.PAGE_SIZE + 1;
    }
    public int getEndRow() {
        return page * PagingConstant.PAGE_SIZE;
    }
}
