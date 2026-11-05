package com.carmanual.carmanual.repositories;
import com.carmanual.carmanual.models.Manual;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ManualRepository extends JpaRepository<Manual, Long> {}
