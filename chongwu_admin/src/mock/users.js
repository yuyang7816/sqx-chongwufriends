const STORAGE_KEY = 'shengqianxiong-users'

const seedUsers = [
  {
    id: 1,
    name: '陈知遥',
    phone: '13800000001',
    email: 'chenzhiyao@example.com',
    status: '启用',
    department: '增长中心',
    balance: 2680.5,
    memberLevel: '铂金会员',
    inviteCode: 'SQX8A21',
    source: '抖音投放',
    inviteCount: 12,
    totalSpend: 18320,
    createdAt: '2024-03-12 10:26:18',
    openid: 'oYxA1cZ-chenzhiyao-2938',
  },
  {
    id: 2,
    name: '吴景衡',
    phone: '13800000002',
    email: 'wujingheng@example.com',
    status: '禁用',
    department: '运营中心',
    balance: 305.8,
    memberLevel: '黄金会员',
    inviteCode: 'SQX7K10',
    source: '公众号裂变',
    inviteCount: 4,
    totalSpend: 6280,
    createdAt: '2024-02-18 15:08:42',
    openid: 'oYxA1cZ-wujingheng-1027',
  },
  {
    id: 3,
    name: '赵清岚',
    phone: '13800000003',
    email: 'zhaoqinglan@example.com',
    status: '启用',
    department: '平台架构',
    balance: 928.36,
    memberLevel: '白银会员',
    inviteCode: 'SQX6P33',
    source: '自然注册',
    inviteCount: 1,
    totalSpend: 2140,
    createdAt: '2024-04-06 09:42:03',
    openid: 'oYxA1cZ-zhaoqinglan-8304',
  },
  {
    id: 4,
    name: '李沐川',
    phone: '13800000004',
    email: 'limuchuan@example.com',
    status: '启用',
    department: '财务管理',
    balance: 12880,
    memberLevel: '黑金会员',
    inviteCode: 'SQX9M62',
    source: '地推活动',
    inviteCount: 25,
    totalSpend: 53200,
    createdAt: '2023-11-25 18:20:11',
    openid: 'oYxA1cZ-limuchuan-5621',
  },
  {
    id: 5,
    name: '孙以宁',
    phone: '13800000005',
    email: 'sunyining@example.com',
    status: '禁用',
    department: '品牌市场',
    balance: 0,
    memberLevel: '普通会员',
    inviteCode: 'SQX2D41',
    source: '小红书内容',
    inviteCount: 0,
    totalSpend: 380,
    createdAt: '2024-05-09 12:03:27',
    openid: 'oYxA1cZ-sunyining-4408',
  },
  {
    id: 6,
    name: '高叙白',
    phone: '13800000006',
    email: 'gaoxubai@example.com',
    status: '启用',
    department: '风险控制',
    balance: 1460.2,
    memberLevel: '黄金会员',
    inviteCode: 'SQX4Q28',
    source: 'App Store 推荐',
    inviteCount: 8,
    totalSpend: 9640,
    createdAt: '2024-01-15 08:31:59',
    openid: 'oYxA1cZ-gaoxubai-6745',
  },
  {
    id: 7,
    name: '何昭宁',
    phone: '13800000007',
    email: 'hezhaoning@example.com',
    status: '启用',
    department: '客户体验',
    balance: 730.12,
    memberLevel: '白银会员',
    inviteCode: 'SQX3L77',
    source: '好友邀请',
    inviteCount: 3,
    totalSpend: 4520,
    createdAt: '2024-06-01 14:17:36',
    openid: 'oYxA1cZ-hezhaoning-9912',
  },
]

function canUseStorage() {
  return typeof window !== 'undefined'
}

export function getUsers() {
  if (!canUseStorage()) {
    return [...seedUsers]
  }

  const raw = window.sessionStorage.getItem(STORAGE_KEY)
  if (!raw) {
    window.sessionStorage.setItem(STORAGE_KEY, JSON.stringify(seedUsers))
    return [...seedUsers]
  }

  try {
    const parsed = JSON.parse(raw)
    return Array.isArray(parsed) ? parsed : [...seedUsers]
  } catch {
    return [...seedUsers]
  }
}

export function saveUsers(users) {
  if (!canUseStorage()) {
    return
  }

  window.sessionStorage.setItem(STORAGE_KEY, JSON.stringify(users))
}

export function getUserById(id) {
  return getUsers().find((item) => item.id === Number(id)) || null
}

export function updateUserBalance(id, balance) {
  const users = getUsers()
  const index = users.findIndex((item) => item.id === Number(id))
  if (index === -1) {
    return null
  }

  users[index] = {
    ...users[index],
    balance: Number(balance),
  }
  saveUsers(users)
  return users[index]
}
