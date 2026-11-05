package com.carmanual.carmanual.controllers;
import com.carmanual.carmanual.models.Manual;
import com.carmanual.carmanual.services.ManualService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/manual")
public class ManualController {
    private final ManualService service;
    public ManualController(ManualService s){service=s;}
    @GetMapping public List<Manual> listar(){return service.listar();}
    @GetMapping("/{id}") public Manual buscar(@PathVariable Long id){return service.buscar(id);}
    @PostMapping public Manual salvar(@RequestBody Manual m){return service.salvar(m);}
    @PutMapping("/{id}") public Manual atualizar(@PathVariable Long id,@RequestBody Manual m){return service.atualizar(id,m);}
    @DeleteMapping("/{id}") public Object excluir(@PathVariable Long id,@RequestParam(defaultValue="false") boolean dryRun){if(dryRun)return service.buscar(id); service.excluir(id); return null;}
}
