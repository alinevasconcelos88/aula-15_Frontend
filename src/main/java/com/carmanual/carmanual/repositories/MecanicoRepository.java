package com.carmanual.carmanual.repositories;
import com.carmanual.carmanual.models.Mecanico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface MecanicoRepository extends JpaRepository<Mecanico, Long> {
    List<Mecanico> findByPlanoId(Long planoId);
}
