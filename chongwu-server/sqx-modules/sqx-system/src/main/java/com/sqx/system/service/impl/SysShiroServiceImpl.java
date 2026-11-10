package com.sqx.system.service.impl;

import com.sqx.common.core.constant.Constant;
import com.sqx.common.shiro.service.ShiroService;
import com.sqx.system.db.mapper.SysMenuDao;
import com.sqx.system.service.SysUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 基于数据库的权限查询，覆盖 common-shiro 中仅从 Redis 菜单树顶层取 perms 的实现。
 */
@Service
@Primary
public class SysShiroServiceImpl implements ShiroService {

    @Resource
    private SysUserService sysUserService;
    @Resource
    private SysMenuDao sysMenuDao;

    @Override
    public Set<String> getUserPermissions(long userId) {
        List<String> permsList;
        if (userId == Constant.SUPER_ADMIN) {
            permsList = sysMenuDao.queryAllMenuPerms();
        } else {
            permsList = sysUserService.queryAllPerms(userId);
        }
        return toPermissionSet(permsList);
    }

    private Set<String> toPermissionSet(List<String> permsList) {
        Set<String> permsSet = new HashSet<>();
        if (permsList == null) {
            return permsSet;
        }
        for (String perms : permsList) {
            if (StringUtils.isBlank(perms)) {
                continue;
            }
            for (String item : perms.split(",")) {
                if (StringUtils.isNotBlank(item)) {
                    permsSet.add(item.trim());
                }
            }
        }
        return permsSet;
    }
}
