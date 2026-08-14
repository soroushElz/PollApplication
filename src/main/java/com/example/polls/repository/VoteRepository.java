package com.example.polls.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.polls.models.ChoiceVoteCount;
import com.example.polls.models.Vote;
import com.example.polls.security.UserPrincipal;

@Repository
public interface VoteRepository extends JpaRepository<Vote,Long> {

	
	@Query("SELECT COUNT(v.id) from Vote v where v.user.Id = :userId")
	long countByUserId(@Param("userId") String id);

	
	@Query("SELECT v.poll.Id from Vote v where v.user.Id = :userId")
	Page<Long> findVotedPollIdByUserId(@Param("userId") String id, Pageable pageable);

    @Query("SELECT v FROM Vote v where v.user.Id=:userId And v.poll.Id=:pollId")
	Vote findByUserIdAndPollId(@Param("userId") String id,@Param("pollId") Long pollId);

   
    @Query("SELECT NEW com.example.polls.models.ChoiceVoteCount(v.choice.id,count(v.id)) FROM "
    		+ "         Vote v where v.poll.Id=:pollId GROUP BY v.choice.id" )
	List<ChoiceVoteCount> countByPollIdGroupByChoiceId(@Param("pollId")Long pollId);

   
    @Query("SELECT NEW com.example.polls.models.ChoiceVoteCount(v.choice.id,count(v.id)) FROM "
    		+ "         Vote v where v.poll.Id in :pollIds GROUP BY v.choice.id" )
	List<ChoiceVoteCount> countByPollIdInGroupByChoiceId(@Param("pollIds") List<Long> pollIds);

	  @Query("SELECT v FROM Vote v where v.user.Id = :userId and v.poll.Id in :pollIds")
	List<Vote> findByUserIdAndpollIdIn(@Param("userId") String id,@Param("pollIds") List<Long> pollIds);

	
}
