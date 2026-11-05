package com.carmanual.carmanual.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carmanual.carmanual.exceptions.GlobalExceptionHandler;
import com.carmanual.carmanual.models.Plano;
import com.carmanual.carmanual.services.ClienteService;
import com.carmanual.carmanual.services.DiagnosticoService;
import com.carmanual.carmanual.services.MecanicoService;
import com.carmanual.carmanual.services.PlanoService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class GlobalExceptionHandlerTest {

    @Test
    void getExistingResourceReturnsJson() throws Exception {
        PlanoService service = mock(PlanoService.class);
        when(service.buscar(1L)).thenReturn(new Plano(1L, "Basico", 19.90));
        MockMvc mvc = planoMvc(service);

        mvc.perform(get("/plano/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Basico"));
    }

    @Test
    void missingResourceReturnsStandardized404() throws Exception {
        PlanoService service = mock(PlanoService.class);
        when(service.buscar(9999L)).thenThrow(
                new EntityNotFoundException("Plano não encontrado com ID: 9999"));
        MockMvc mvc = planoMvc(service);

        mvc.perform(get("/plano/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Plano não encontrado com ID: 9999"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void invalidRequestReturnsDetailed400BeforeCallingService() throws Exception {
        DiagnosticoService service = mock(DiagnosticoService.class);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new DiagnosticoController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mvc.perform(post("/diagnostico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\":\"\",\"resultado\":\"\",\"idVeic\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("descricao")))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());

        verifyNoInteractions(service);
    }

    @Test
    void unexpectedExceptionReturnsStandardized500() throws Exception {
        PlanoService service = mock(PlanoService.class);
        when(service.buscar(1L)).thenThrow(new IllegalStateException("erro interno"));
        MockMvc mvc = planoMvc(service);

        mvc.perform(get("/plano/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Erro interno do servidor."))
                .andExpect(jsonPath("$.timestamp").isString());
    }

    private MockMvc planoMvc(PlanoService service) {
        return MockMvcBuilders.standaloneSetup(new PlanoController(
                        service, mock(ClienteService.class), mock(MecanicoService.class)))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }
}