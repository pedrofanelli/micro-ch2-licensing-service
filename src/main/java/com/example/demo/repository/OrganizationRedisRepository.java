package com.example.demo.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Organization;

@Repository
public interface OrganizationRedisRepository extends CrudRepository<Organization,String> {

}
