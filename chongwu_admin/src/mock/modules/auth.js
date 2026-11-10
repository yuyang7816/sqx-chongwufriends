const accounts = {
  admin: {
    password: '123456',
    profile: { name: '林越', roleCode: 'SUPER_ADMIN', roleName: '超级管理员', email: 'admin@example.com' },
    permissions: ['user:view', 'user:create', 'user:update', 'user:delete', 'admin:view', 'admin:create', 'admin:update', 'admin:delete', 'role:view', 'role:create', 'role:update', 'role:delete', 'menu:view', 'menu:create', 'menu:update', 'menu:delete', 'dict:view', 'dict:create', 'dict:update', 'dict:delete', 'category:view', 'category:create', 'category:update', 'category:delete', 'decoration:view', 'decoration:create', 'decoration:update', 'decoration:delete'],
  },
  operator: {
    password: '123456',
    profile: { name: '周衡', roleCode: 'OPS_ADMIN', roleName: '运营主管', email: 'operator@example.com' },
    permissions: ['user:view', 'user:update', 'admin:view', 'role:view', 'menu:view', 'dict:view', 'category:view', 'category:update', 'decoration:view', 'decoration:update'],
  },
}

export function mockLogin({ username, password }) {
  const account = accounts[username]
  if (!account || account.password !== password) {
    throw new Error('账号或密码错误')
  }
  return {
    token: `mock-token-${Date.now()}`,
    refreshToken: `mock-refresh-${Date.now()}`,
    expiresAt: Date.now() + 2 * 60 * 60 * 1000,
    profile: account.profile,
    permissions: account.permissions,
  }
}
