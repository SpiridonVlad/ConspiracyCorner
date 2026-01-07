// TypeScript types matching the GraphQL schema

export enum TheoryStatus {
  UNVERIFIED = 'UNVERIFIED',
  DEBUNKED = 'DEBUNKED',
  CONFIRMED = 'CONFIRMED',
}

export interface User {
  id: string;
  username: string;
  email: string;
  anonymousMode: boolean;
  createdAt: string;
  theories?: Theory[];
  comments?: Comment[];
}

export interface Theory {
  id: string;
  title: string;
  content: string;
  status: TheoryStatus;
  evidenceUrls: string[];
  postedAt: string;
  updatedAt?: string;
  isAnonymousPost: boolean;
  author?: User;
  authorName: string;
  comments: Comment[];
  commentCount: number;
}

export interface Comment {
  id: string;
  content: string;
  postedAt: string;
  updatedAt?: string;
  isAnonymousPost: boolean;
  author?: User;
  authorName: string;
  theory: Theory;
}

export interface AuthResponse {
  token: string;
  username: string;
  userId: string;
  message: string;
}

export interface TheoriesPage {
  content: Theory[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface TheoryFilter {
  status?: TheoryStatus;
  keyword?: string;
  hotOnly?: boolean;
  minCommentCount?: number;
}

export interface PageInput {
  page: number;
  size: number;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  secretCode?: string;
}

export interface LoginRequest {
  username: string;
  password: string;
  secretCode?: string;
}

export interface TheoryInput {
  title: string;
  content: string;
  status?: TheoryStatus;
  evidenceUrls?: string[];
  anonymousPost?: boolean;
}

export interface CommentInput {
  content: string;
  theoryId: string;
  anonymousPost?: boolean;
}
