package com.penguinwebstudio.building;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class BuildingService {

	BuildingRepository buildingRepository;
	PendingAdditionRepository pendingAdditionRepository;
	
	@Autowired
	public BuildingService(BuildingRepository repository, PendingAdditionRepository pending) {
		this.buildingRepository = repository;
		this.pendingAdditionRepository = pending;
	}
	
	public Optional<Building> getBuildingById(Long id) {
		return buildingRepository.findById(id);
	}
	
	public Optional<PendingAddition> getPendingById(Long id) {
		return pendingAdditionRepository.findById(id);
	}
	
	public void addBuildingPending(PendingAddition building, boolean newImage) {
		Building existingBuilding = buildingRepository.findByName(building.getName());
		PendingAddition existingPending = pendingAdditionRepository.findByName(building.getName());
		if (existingPending != null && existingPending.getName().equals(building.getName())) {
			existingPending.setName(building.getName());
			existingPending.setOccupancy(building.getOccupancy());
			existingPending.setType(building.getType());
			existingPending.setTiles(building.getTiles());
			existingPending.setStyle(building.getStyle());
			existingPending.setLastUpdated(building.getLastUpdated());
			existingPending.setApproved(building.getApproved());
			existingPending.setDeletion(building.getDeletion());
			if (!newImage && existingBuilding != null) {
				existingPending.setImage(existingBuilding.getImage());
			} else {
				existingPending.setImage(building.getImage());
			}
			pendingAdditionRepository.save(existingPending);
		} else {
			if (!newImage && existingBuilding != null) {
				building.setImage(existingBuilding.getImage());
			}
			pendingAdditionRepository.save(building);
		}
	}
	
	public boolean setBuildingAddition(Long id) {
		PendingAddition existingPending = pendingAdditionRepository.findById(id).orElse(null);
		if (existingPending == null) {
			return false;
		}
		Building existingBuilding = buildingRepository.findByName(existingPending.getName());
		if (existingBuilding == null || !existingBuilding.getName().equals(existingPending.getName())) {
			Building newBuilding = new Building(
					existingPending.getId(),
					existingPending.getName(),
					existingPending.getOccupancy(),
					existingPending.getType(),
					existingPending.getTiles(),
					existingPending.getStyle()
			);
			newBuilding.setApproved(true);
			newBuilding.setDeletion(false);
			newBuilding.setImage(existingPending.getImage());
			buildingRepository.save(newBuilding);
		} else {
			existingBuilding.setName(existingPending.getName());
			existingBuilding.setOccupancy(existingPending.getOccupancy());
			existingBuilding.setType(existingPending.getType());
			existingBuilding.setTiles(existingPending.getTiles());
			existingBuilding.setStyle(existingPending.getStyle());
			existingBuilding.setLastUpdated(existingPending.getLastUpdated());
			existingBuilding.setApproved(true);
			existingBuilding.setDeletion(false);
			existingBuilding.setImage(existingPending.getImage());
			buildingRepository.save(existingBuilding);
		}
		pendingAdditionRepository.deleteById(existingPending.getId());
		return true;
	}
	
	public void unsetBuildingAddition(Long id) {
		pendingAdditionRepository.deleteById(id);
	}
	
	public Building getBuildingByName(String name) {
		return buildingRepository.findByName(name);
	}
	
	public Page<Building> getPagedBuildings(Pageable pageable) {
		return buildingRepository.findAllByApproved(true, pageable);
	}
	
	public List<Building> getAllBuildings() {
		return buildingRepository.findAllByApprovedOrderByNameAsc(true);
	}
	
	public List<Building> getAllBuildingsByDeletion() {
		return buildingRepository.findAllByDeletionOrderByLastUpdatedDesc(true);
	}
	
	public List<PendingAddition> getAllPendingBuildings() {
		return pendingAdditionRepository.findAllByOrderByLastUpdatedDesc();
	}
	
	public PendingAddition getPendingBuildingById(Long id) {
		return pendingAdditionRepository.findById(id).orElse(null);
	}
	
	public Building setBuildingDeletion(Long id) {
		Building building = buildingRepository.findById(id).orElse(null);
		if (building == null) { 
			return null;
		}
		building.setDeletion(true);
		buildingRepository.save(building);
		return building;
	}
	
	public Building unsetBuildingDeletion(Long id) {
		Building building = buildingRepository.findById(id).orElse(null);
		if (building == null) { 
			return null;
		}
		building.setDeletion(false);
		buildingRepository.save(building);
		return building;
	}
	
	public void deleteBuildingById(Long id) {
		buildingRepository.deleteById(id);
	}
	
	public long getCount() {
		return buildingRepository.count();
	}
	
}
