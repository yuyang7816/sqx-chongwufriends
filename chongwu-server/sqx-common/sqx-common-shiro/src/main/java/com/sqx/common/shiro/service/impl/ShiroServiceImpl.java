package com.sqx.common.shiro.service.impl;


import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.sqx.common.core.constant.Constant;
import com.sqx.common.redis.utils.RedisUtil;
import com.sqx.common.shiro.constant.ShiroConstant;
import com.sqx.common.shiro.model.AuthSysMenu;
import com.sqx.common.shiro.service.ShiroService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author chengming
 * @since 2024/7/11
 */
@Service
public class ShiroServiceImpl implements ShiroService {
    @Override
    public Set<String> getUserPermissions(long userId) {
        List<String> permsList = new ArrayList<>();

        //系统管理员，拥有最高权限
        if (userId == Constant.SUPER_ADMIN) {
            String menuListJson = RedisUtil.getCacheObject(ShiroConstant.AUTH_SYS_MENU);
            collectMenuPerms(parseMenuList(menuListJson), permsList);
        } else {
            String menuListJson = RedisUtil.getCacheObject(String.format(ShiroConstant.AUTH_USER_MENU, userId));
            collectMenuPerms(parseMenuList(menuListJson), permsList);
        }

        Set<String> permsSet = new HashSet<>();
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

    private List<AuthSysMenu> parseMenuList(String menuListJson) {
        if (StringUtils.isBlank(menuListJson)) {
            return Collections.emptyList();
        }
        List<AuthSysMenu> menuList = JSONObject.parseObject(menuListJson, new TypeReference<List<AuthSysMenu>>() {});
        return menuList == null ? Collections.emptyList() : menuList;
    }

    private void collectMenuPerms(List<AuthSysMenu> menuList, List<String> permsList) {
        if (menuList == null || menuList.isEmpty()) {
            return;
        }
        for (AuthSysMenu menu : menuList) {
            if (menu == null) {
                continue;
            }
            if (StringUtils.isNotBlank(menu.getPerms())) {
                permsList.add(menu.getPerms());
            }
            Object children = menu.getList();
            if (children instanceof List) {
                collectMenuPerms((List<AuthSysMenu>) children, permsList);
            }
        }
    }
}
