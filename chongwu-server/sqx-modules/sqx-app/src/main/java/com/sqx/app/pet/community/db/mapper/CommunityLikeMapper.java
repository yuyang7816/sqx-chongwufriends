package com.sqx.app.pet.community.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sqx.app.pet.community.domain.entity.CommunityLike;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface CommunityLikeMapper extends BaseMapper<CommunityLike> {

    @Select("SELECT id, post_id, user_id, create_time, update_time, is_deleted " +
            "FROM community_like WHERE post_id = #{postId} AND user_id = #{userId} LIMIT 1")
    CommunityLike selectAnyByPostAndUser(@Param("postId") Long postId, @Param("userId") Long userId);

    @Update("UPDATE community_like SET is_deleted = 0, update_time = NOW() WHERE id = #{id}")
    int restoreById(@Param("id") Long id);

    @Delete("DELETE FROM community_like WHERE post_id = #{postId} AND user_id = #{userId}")
    int physicalDeleteByPostAndUser(@Param("postId") Long postId, @Param("userId") Long userId);

    @Delete("DELETE FROM community_like WHERE post_id = #{postId}")
    int physicalDeleteByPostId(@Param("postId") Long postId);

    @Select("<script>SELECT post_id AS postId, COUNT(*) AS cnt FROM community_like WHERE is_deleted=0 AND post_id IN "
            + "<foreach collection='postIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> "
            + "GROUP BY post_id</script>")
    List<Map<String, Object>> countGroupByPostIds(@Param("postIds") List<Long> postIds);
}
