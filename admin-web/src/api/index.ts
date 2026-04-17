import request from '../utils/request'

export const login = (username: string) => {
  return request({
    url: '/auth/login/admin',
    method: 'post',
    params: { username }
  })
}

export const syncRecord = (data: any) => {
  return request({
    url: '/admin/leaderboard/sync',
    method: 'post',
    data
  })
}

export const forceScore = (key: string, userId: string, score: number) => {
  return request({
    url: '/admin/leaderboard/force-score',
    method: 'put',
    params: { key, userId, score }
  })
}

export const disasterRecovery = (gameId: number) => {
  return request({
    url: `/admin/leaderboard/disaster-recovery/${gameId}`,
    method: 'post'
  })
}

// 游戏接口
export const fetchGames = (page: number = 1, size: number = 10) => {
  return request({
    url: '/admin/games',
    method: 'get',
    params: { page, size }
  })
}

export const createGame = (data: any) => {
  return request({
    url: '/admin/games',
    method: 'post',
    data
  })
}

export const updateGame = (id: number, data: any) => {
  return request({
    url: `/admin/games/${id}`,
    method: 'put',
    data
  })
}

// 用户接口
export const fetchUsers = (keyword?: string, page: number = 1, size: number = 10) => {
  return request({
    url: '/admin/users',
    method: 'get',
    params: { keyword, page, size }
  })
}

export const updateUser = (id: number, data: any) => {
  return request({
    url: `/admin/users/${id}`,
    method: 'put',
    data
  })
}

// 记录接口
export const fetchRecords = (userId?: string, page: number = 1, size: number = 10) => {
  return request({
    url: '/admin/records',
    method: 'get',
    params: { userId, page, size }
  })
}

export const deleteRecord = (id: number) => {
  return request({
    url: `/admin/records/${id}`,
    method: 'delete'
  })
}
