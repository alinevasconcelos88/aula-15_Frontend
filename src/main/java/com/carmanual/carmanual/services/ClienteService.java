package com.carmanual.carmanual.services;
import com.carmanual.carmanual.models.Cliente;
import com.carmanual.carmanual.models.Plano;
import com.carmanual.carmanual.repositories.ClienteRepository;
import com.carmanual.carmanual.repositories.PlanoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
@Service
public class ClienteService {
    private final ClienteRepository repo; private final PlanoRepository planoRepo;
    public ClienteService(ClienteRepository repo, PlanoRepository planoRepo) { this.repo=repo; this.planoRepo=planoRepo; }
    public List<Cliente> listar(){return repo.findAll();}
    public Cliente buscar(Long id){return repo.findById(id).orElseThrow(() -> naoEncontrado("Cliente", id));}
    public Cliente salvar(Cliente c, Long idPlan){
        if(idPlan != null) {
            Plano plano = planoRepo.findById(idPlan)
                    .orElseThrow(() -> naoEncontrado("Plano", idPlan));
            c.setPlano(plano);
        }
        return repo.save(c);
    }
    public Cliente atualizar(Long id, Cliente c){buscar(id); c.setId(id); return repo.save(c);}
    public void excluir(Long id){buscar(id); repo.deleteById(id);}
    public List<Cliente> porPlano(Long id){return repo.findByPlanoId(id);}
    private EntityNotFoundException naoEncontrado(String entidade, Long id){return new EntityNotFoundException(entidade + " não encontrado com ID: " + id);}
}
