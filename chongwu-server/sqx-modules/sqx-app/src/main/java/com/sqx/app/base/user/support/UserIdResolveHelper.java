package com.sqx.app.base.user.support;

import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理端按手机号/用户名解析 userId。
 * 返回 null 表示不按用户过滤；空列表表示条件无匹配用户。
 */
@Component
public class UserIdResolveHelper {

    @Resource
    private IUserService iUserService;

    /**
     * @param userId   内部使用（如用户详情 Tab），对运营不可见
     * @param phone    手机号精确匹配
     * @param username 用户名/昵称模糊匹配
     */
    public List<Long> resolveOptional(Long userId, String phone, String username) {
        if (userId != null) {
            return Collections.singletonList(userId);
        }
        String phoneTrim = StringUtils.trimToNull(phone);
        String nameTrim = StringUtils.trimToNull(username);
        if (phoneTrim == null && nameTrim == null) {
            return null;
        }
        List<UserEntity> users = iUserService.lambdaQuery()
                .eq(phoneTrim != null, UserEntity::getPhone, phoneTrim)
                .like(nameTrim != null, UserEntity::getUserName, nameTrim)
                .select(UserEntity::getUserId)
                .last("LIMIT 500")
                .list();
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }
        return users.stream().map(UserEntity::getUserId).collect(Collectors.toList());
    }
}
