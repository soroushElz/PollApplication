package com.example.polls.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.polls.models.Poll;

@Repository
public interface PollRepository extends JpaRepository<Poll,Long> {

    long countByCreatedBy(String userId);

	List<Poll> findByIdIn(List<Long> pollIds);
	
	List<Poll> findByIdIn(List<Long> pollIds, Sort sort);
	
	Optional<Poll> getPollById(Long pollId);

	 Page<Poll> findByCreatedBy(String createdBy, Pageable pageable);
}
