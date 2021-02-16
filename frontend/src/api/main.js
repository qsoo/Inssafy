import http from './http';

//Favorite
//즐겨찾기 리스트 가져오기
export function getFavorites(user_id) {
  return http.get(`/main/selectFavorite/${user_id}`);
}

//Popular - board
//팔로우 수 기준
export function getFollowRank() {
  return http.get('/main/getFollowRank');
}
//게시글 수 기준
export function getPostRank() {
  return http.get('/main/getBoardPostRank');
}

//popular - post
//좋아요 수 기준
export function getLikeRank() {
  return http.get('/main/getPostLikeRank');
}
//댓글 수 기준
export function getCommentRank() {
  return http.get('/main/getCommentRank');
}

//suggest 배열을 만들 최신보드 16개 가져오기
export function getNewBoard() {
  return http.get(`/board/getBoards?page=1&size=16&sort=new`);
}