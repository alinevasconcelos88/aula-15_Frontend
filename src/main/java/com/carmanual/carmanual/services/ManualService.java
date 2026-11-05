package com.carmanual.carmanual.services;
import jakarta.persistence.EntityNotFoundException;
import com.carmanual.carmanual.models.Manual;
import com.carmanual.carmanual.repositories.ManualRepository;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class ManualService {
    private final ManualRepository repo;
    public ManualService(ManualRepository repo){this.repo=repo;}
    public List<Manual> listar(){return repo.findAll();}
    public Manual buscar(Long id){return repo.findById(id).orElseThrow(() -> naoEncontrado(id));}
    public Manual salvar(Manual m){return repo.save(m);}
    public Manual atualizar(Long id,Manual m){buscar(id);m.setId(id);return repo.save(m);}
    public void excluir(Long id){buscar(id);repo.deleteById(id);}
    private EntityNotFoundException naoEncontrado(Long id){return new EntityNotFoundException("Manual não encontrado com ID: " + id);}
}
