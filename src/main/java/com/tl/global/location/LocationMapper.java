package com.tl.global.location;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LocationMapper {

	public int insert(LocationVO.Insert locationInsert);

	public int delete(int locationNo);

}
