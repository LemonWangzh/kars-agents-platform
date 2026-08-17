package com.kars.mapper;

import com.kars.entity.ChatMemoryRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ChatMemoryMapper {

    ChatMemoryRecord selectByUserId(@Param("userId") String userId);

    int insert(ChatMemoryRecord record);

    int updateById(ChatMemoryRecord record);

    int deleteByUserId(@Param("userId") String userId);
}
