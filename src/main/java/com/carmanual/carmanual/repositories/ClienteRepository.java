package com.carmanual.carmanual.repositories;
import com.carmanual.carmanual.models.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findByPlanoId(Long planoId);
}
