package com.carmanual.carmanual.controllers;
import com.carmanual.carmanual.models.Avaliacao;
import com.carmanual.carmanual.services.AvaliacaoService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/avaliacao")
public class AvaliacaoController {
    private final AvaliacaoService service;
    public AvaliacaoController(AvaliacaoService s){service=s;}
    @GetMapping public List<Avaliacao> listar(){return service.listar();}
    @GetMapping("/{id}") public Avaliacao buscar(@PathVariable Long id){return service.buscar(id);}
    @PostMapping public Avaliacao salvar(@RequestBody Avaliacao a,@RequestParam(required=false) Long clienteId,@RequestParam(required=false) Long mecanicoId){return service.salvar(a,clienteId,mecanicoId);}
    @PutMapping("/{id}") public Avaliacao atualizar(@PathVariable Long id,@RequestBody Avaliacao a,@RequestParam(required=false) Long clienteId,@RequestParam(required=false) Long mecanicoId){return service.atualizar(id,a,clienteId,mecanicoId);}
    @DeleteMapping("/{id}") public Object excluir(@PathVariable Long id,@RequestParam(defaultValue="false") boolean dryRun){if(dryRun)return service.buscar(id); service.excluir(id); return null;}
}
