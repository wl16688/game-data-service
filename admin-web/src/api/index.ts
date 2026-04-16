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

// NOTE: Since the backend doesn't have standard CRUD endpoints for Games/Users/Records implemented yet (except those we just created for DataService), 
// we will mock the CRUD list responses or point them to placeholders for now.

export const fetchGames = () => {
  // Placeholder
  return Promise.resolve([
    { id: 1, code: 'sheep_game', name: '羊了个羊', description: '休闲益智' },
    { id: 2, code: 'tower_defense', name: '塔防大战', description: '策略防守' }
  ])
}

export const fetchUsers = () => {
  // Placeholder
  return Promise.resolve([
    { id: 1, openid: 'oWxMock_001', nickname: '深圳玩家A', countryId: 1, provinceId: 440000 },
    { id: 2, openid: 'oWxMock_002', nickname: '广州玩家B', countryId: 1, provinceId: 440000 }
  ])
}

export const fetchRecords = () => {
  // Placeholder
  return Promise.resolve([
    { id: 1, userId: '1', gameId: 1, levelId: 'level_1', timestamp: Date.now() },
    { id: 2, userId: '2', gameId: 1, levelId: 'level_2', timestamp: Date.now() - 3600000 }
  ])
}
