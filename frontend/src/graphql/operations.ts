import { gql } from '@apollo/client';

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
    score
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
    score
    authorName
    author {
      id
      reputation
    }
  }
`;

export const COMMENT_WITH_REPLIES_FRAGMENT = gql`
  fragment CommentWithReplies on Comment {
    ...CommentFields
    replies {
      ...CommentFields
      replies {
        ...CommentFields
        replies {
          ...CommentFields
        }
      }
    }
  }
  ${COMMENT_FRAGMENT}
`;

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
      author {
        id
        reputation
      }
    }
    rootCommentsByTheory(theoryId: $id) {
      ...CommentWithReplies
    }
  }
  ${THEORY_FRAGMENT}
  ${COMMENT_WITH_REPLIES_FRAGMENT}
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

export const GET_ME = gql`
  query GetMe {
    me {
      id
      username
      email
      anonymousMode
      reputation
      createdAt
    }
  }
`;

export const REGISTER_USER = gql`
  mutation Register($input: RegisterRequest!) {
    register(input: $input) {
      token
      username
      userId
      message
      mustChangePassword
    }
  }
`;

export const LOGIN_USER = gql`
  mutation Login($input: LoginRequest!) {
    login(input: $input) {
      token
      username
      userId
      message
      mustChangePassword
    }
  }
`;

export const CHANGE_PASSWORD = gql`
  mutation ChangePassword($input: ChangePasswordRequest!) {
    changePassword(input: $input)
  }
`;

export const FORGOT_PASSWORD = gql`
  mutation ForgotPassword($input: ForgotPasswordRequest!) {
    forgotPassword(input: $input)
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

export const VOTE_THEORY = gql`
  mutation VoteTheory($id: ID!, $value: Int!) {
    voteTheory(id: $id, value: $value) {
      id
      score
    }
  }
`;

export const VOTE_COMMENT = gql`
  mutation VoteComment($id: ID!, $value: Int!) {
    voteComment(id: $id, value: $value) {
      id
      score
    }
  }
`;
