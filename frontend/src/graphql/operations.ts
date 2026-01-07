import { gql } from '@apollo/client';

// Fragments
export const THEORY_FRAGMENT = gql`
  fragment TheoryFields on Theory {
    id
    title
    content
    status
    evidenceUrls
    postedAt
    updatedAt
    isAnonymousPost
    authorName
    commentCount
  }
`;

export const COMMENT_FRAGMENT = gql`
  fragment CommentFields on Comment {
    id
    content
    postedAt
    updatedAt
    isAnonymousPost
    authorName
  }
`;

// Queries
export const GET_THEORIES = gql`
  query GetTheories($filter: TheoryFilter, $page: PageInput) {
    theories(filter: $filter, page: $page) {
      ...TheoryFields
    }
  }
  ${THEORY_FRAGMENT}
`;

export const GET_THEORIES_PAGINATED = gql`
  query GetTheoriesPaginated($filter: TheoryFilter, $page: PageInput) {
    theoriesPaginated(filter: $filter, page: $page) {
      content {
        ...TheoryFields
      }
      totalElements
      totalPages
      currentPage
      hasNext
      hasPrevious
    }
  }
  ${THEORY_FRAGMENT}
`;

export const GET_THEORY = gql`
  query GetTheory($id: ID!) {
    theory(id: $id) {
      ...TheoryFields
      comments {
        ...CommentFields
      }
    }
  }
  ${THEORY_FRAGMENT}
  ${COMMENT_FRAGMENT}
`;

export const GET_THEORIES_BY_USER = gql`
  query GetTheoriesByUser($userId: ID!) {
    theoriesByUser(userId: $userId) {
      ...TheoryFields
    }
  }
  ${THEORY_FRAGMENT}
`;

export const GET_HOT_THEORIES = gql`
  query GetHotTheories($page: PageInput) {
    hotTheories(page: $page) {
      ...TheoryFields
    }
  }
  ${THEORY_FRAGMENT}
`;

export const GET_COMMENTS_BY_THEORY = gql`
  query GetCommentsByTheory($theoryId: ID!, $page: PageInput) {
    commentsByTheory(theoryId: $theoryId, page: $page) {
      ...CommentFields
    }
  }
  ${COMMENT_FRAGMENT}
`;

export const GET_ME = gql`
  query GetMe {
    me {
      id
      username
      email
      anonymousMode
      createdAt
    }
  }
`;

// Mutations
export const REGISTER = gql`
  mutation Register($input: RegisterRequest!) {
    register(input: $input) {
      token
      username
      userId
      message
    }
  }
`;

export const LOGIN = gql`
  mutation Login($input: LoginRequest!) {
    login(input: $input) {
      token
      username
      userId
      message
    }
  }
`;

export const CREATE_THEORY = gql`
  mutation CreateTheory($input: TheoryInput!) {
    createTheory(input: $input) {
      ...TheoryFields
    }
  }
  ${THEORY_FRAGMENT}
`;

export const UPDATE_THEORY = gql`
  mutation UpdateTheory($id: ID!, $input: TheoryInput!) {
    updateTheory(id: $id, input: $input) {
      ...TheoryFields
    }
  }
  ${THEORY_FRAGMENT}
`;

export const DELETE_THEORY = gql`
  mutation DeleteTheory($id: ID!) {
    deleteTheory(id: $id)
  }
`;

export const CREATE_COMMENT = gql`
  mutation CreateComment($input: CommentInput!) {
    createComment(input: $input) {
      ...CommentFields
    }
  }
  ${COMMENT_FRAGMENT}
`;

export const UPDATE_COMMENT = gql`
  mutation UpdateComment($id: ID!, $content: String!) {
    updateComment(id: $id, content: $content) {
      ...CommentFields
    }
  }
  ${COMMENT_FRAGMENT}
`;

export const DELETE_COMMENT = gql`
  mutation DeleteComment($id: ID!) {
    deleteComment(id: $id)
  }
`;

export const SET_ANONYMOUS_MODE = gql`
  mutation SetAnonymousMode($anonymous: Boolean!) {
    setAnonymousMode(anonymous: $anonymous) {
      id
      anonymousMode
    }
  }
`;
