package com.penguinwebstudio.building;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface BuildingRepository extends CrudRepository<Building, Long> {

	public Optional<Building> findById(Long id);
	
	public Building findByName(String name);
	
	public List<Building> findAllByApprovedOrderByNameAsc(boolean approved);
	
	public Page<Building> findAllByApproved(boolean approved, Pageable pageable);
	
	public List<Building> findAllByDeletionOrderByLastUpdatedDesc(boolean deletion);
	
	public void deleteById(Long id);
	
	public long count();
	
}
