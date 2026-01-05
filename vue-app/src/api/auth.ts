import request from '@/utils/request'

export interface LoginData {
  username: string
  password: string
  captcha?: string
}

export interface LoginResult {
  token: string
}

/**
 * 用户登录
 */
export function login(data: LoginData) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

/**
 * 获取用户信息
 */
export function getUserInfo() {
  return request({
    url: '/auth/userInfo',
    method: 'get'
  })
}

/**
 * 用户登出
 */
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}