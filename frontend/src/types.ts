/**
 * Frontend TypeScript models mirroring the backend entities.
 * Money fields are euro cents; `score` is average rating x10.
 */

export type Role = 'USER' | 'MERCHANT' | 'ADMIN'

export interface ShopType {
  id: number
  name: string
  icon: string
  sort: number
}

export interface Shop {
  id: number
  name: string
  typeId: number
  ownerId?: number
  images: string
  area: string
  address: string
  x?: number
  y?: number
  avgPrice?: number
  sold: number
  comments: number
  score: number
  openHours: string
}

export interface Voucher {
  id: number
  shopId: number
  title: string
  subTitle: string
  rules: string
  payValue: number
  actualValue: number
  type: number
  stock: number
  status: number
  beginTime?: string
  endTime?: string
}

export interface Blog {
  id: number
  userId: number
  shopId?: number
  title: string
  images: string
  content: string
  liked: number
  comments: number
  featured: number
  status: number
  authorName?: string
  authorIcon?: string
  likedByCurrentUser?: boolean
  createTime?: string
}

export interface ShopRating {
  id: number
  shopId: number
  userId: number
  score: number
  content: string
  createTime?: string
}

export interface BlogComment {
  id: number
  userId: number
  blogId: number
  parentId: number
  answerId: number
  content: string
  liked: number
  authorName?: string
  authorIcon?: string
  createTime?: string
}

export interface LoginResponse {
  token: string
  userId: number
  nickName: string
  role: Role
}

export interface UserStats {
  following: number
  followers: number
  experience: number
  proThreshold: number
}

export interface VoucherOrder {
  id: number
  userId: number
  voucherId: number
  payType: number
  /** 1 unpaid, 2 paid, 3 used, 4 cancelled, 5 refunded. */
  status: number
  createTime?: string
  payTime?: string
  voucherTitle?: string
  shopName?: string
}
