package com.sqx.app.pet.community.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sqx.app.pet.community.domain.entity.CommunityComment;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface CommunityCommentMapper extends BaseMapper<CommunityComment> {

    @Select("<script>SELECT post_id AS postId, COUNT(*) AS cnt FROM community_comment WHERE is_deleted=0 AND post_id IN "
            + "<foreach collection='postIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> "
            + "GROUP BY post_id</script>")
    List<Map<String, Object>> countGroupByPostIds(@Param("postIds") List<Long> postIds);
}
