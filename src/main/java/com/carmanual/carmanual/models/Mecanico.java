package com.carmanual.carmanual.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mecanico")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Mecanico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private String email;
    private String especialidade;
    private Double avaliacaoMedia = 0.0;

    @ManyToOne
    @JoinColumn(name = "id_plan")
    private Plano plano;

}
