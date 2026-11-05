package com.carmanual.carmanual.services;
import jakarta.persistence.EntityNotFoundException;
import com.carmanual.carmanual.models.Plano;
import com.carmanual.carmanual.repositories.PlanoRepository;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class PlanoService {
    private final PlanoRepository repo;
    public PlanoService(PlanoRepository repo) { this.repo = repo; }
    public List<Plano> listar() { return repo.findAll(); }
    public Plano buscar(Long id) { return repo.findById(id).orElseThrow(() -> naoEncontrado(id)); }
    public Plano salvar(Plano p) { return repo.save(p); }
    public Plano atualizar(Long id, Plano p) { buscar(id); p.setId(id); return repo.save(p); }
    public void excluir(Long id) { buscar(id); repo.deleteById(id); }
    private EntityNotFoundException naoEncontrado(Long id) { return new EntityNotFoundException("Plano não encontrado com ID: " + id); }
}
