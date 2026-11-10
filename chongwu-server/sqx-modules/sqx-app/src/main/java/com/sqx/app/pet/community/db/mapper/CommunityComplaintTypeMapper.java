package com.sqx.app.pet.community.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sqx.app.pet.community.domain.entity.CommunityComplaintType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CommunityComplaintTypeMapper extends BaseMapper<CommunityComplaintType> {

    @Select("SELECT id, name, sort, status, create_time, update_time, is_deleted "
            + "FROM community_complaint_type WHERE name = #{name} LIMIT 1")
    CommunityComplaintType selectByNameIncludeDeleted(@Param("name") String name);

    @Update("UPDATE community_complaint_type SET is_deleted = 0, sort = #{sort}, status = #{status}, update_time = NOW() "
            + "WHERE id = #{id}")
    int restoreById(@Param("id") Long id, @Param("sort") Integer sort, @Param("status") Integer status);

    @Delete("DELETE FROM community_complaint_type WHERE id = #{id}")
    int physicalDeleteById(@Param("id") Long id);
}
