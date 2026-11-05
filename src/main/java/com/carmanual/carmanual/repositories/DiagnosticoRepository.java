package com.carmanual.carmanual.repositories;
import com.carmanual.carmanual.models.Diagnostico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface DiagnosticoRepository extends JpaRepository<Diagnostico, Long> {
    List<Diagnostico> findByClienteId(Long clienteId);
}
