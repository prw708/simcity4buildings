package com.penguinwebstudio.building;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface PendingAdditionRepository extends CrudRepository<PendingAddition, Long> {

	public Optional<PendingAddition> findById(Long id);
	
	public PendingAddition findByName(String name);
	
	public List<PendingAddition> findAllByOrderByNameDesc();
	
	public List<PendingAddition> findAllByOrderByOccupancyDesc();
	
	public List<PendingAddition> findAllByOrderByLastUpdatedDesc();
	
	public void deleteById(Long id);
	
	public long count();
	
}
