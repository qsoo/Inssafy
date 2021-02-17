package com.ssafy.pjt1.model.mapper;

import java.util.List;
import java.util.Map;

import com.ssafy.pjt1.model.dto.vote.VoteDto;
import com.ssafy.pjt1.model.dto.vote.VoteItemDto;
import com.ssafy.pjt1.model.dto.vote.VoteSelectDto;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VoteMapper {

	public void createVote(VoteDto voteDto);

	public int voteModify(VoteDto voteDto);

	public int voteDelete(int vote_id);

	public void createVoteItem(VoteItemDto voteItemDto);

	public int voteItemModify(VoteItemDto voteItemDto);

	public int voteItemDelete(int vote_item_id);

	public void selectVote(VoteSelectDto voteSelectDto);

	public VoteDto getVoteById(int vote_id);

	public List<Map<String, Object>> getVoteItem(int vote_id);

	public void voteDeleteAll(int vote_id);

	public List<Integer> getBoardVote(int board_id);

	public int isVoted(Map<String, Object> map);
    
}

