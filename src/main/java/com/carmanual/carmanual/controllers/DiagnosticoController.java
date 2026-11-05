package com.carmanual.carmanual.controllers;
import com.carmanual.carmanual.models.Diagnostico;
import com.carmanual.carmanual.services.DiagnosticoService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
@RestController @RequestMapping("/diagnostico")
public class DiagnosticoController {
    private final DiagnosticoService service;
    public DiagnosticoController(DiagnosticoService s){service=s;}
    @GetMapping public List<Diagnostico> listar(){return service.listar();}
    @GetMapping("/{id}") public Diagnostico buscar(@PathVariable Long id){return service.buscar(id);}
    @PostMapping public Diagnostico salvar(@Valid @RequestBody Diagnostico d,@RequestParam(required=false) Long idCliente,@RequestParam(required=false) Long idManual){return service.salvar(d,idCliente,idManual);}
    @PutMapping("/{id}") public Diagnostico atualizar(@PathVariable Long id,@Valid @RequestBody Diagnostico d){return service.atualizar(id,d);}
    @DeleteMapping("/{id}") public Object excluir(@PathVariable Long id,@RequestParam(defaultValue="false") boolean dryRun){if(dryRun)return service.buscar(id); service.excluir(id); return null;}
    @GetMapping("/{id}/manual") public Object manual(@PathVariable Long id){return service.manual(id);}
}
