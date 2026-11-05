INSERT INTO app_user (id, username)
SELECT 1, 'Usuario 1'
WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE id = 1);

INSERT INTO avaliacao (nota, comentario, cliente_id, mecanico_id)
SELECT 5, 'Excelente atendimento', MIN(c.id), MIN(m.id)
FROM cliente c
CROSS JOIN mecanico m
WHERE NOT EXISTS (
    SELECT 1
    FROM avaliacao a
    WHERE a.comentario = 'Excelente atendimento'
      AND a.cliente_id = (SELECT MIN(id) FROM cliente)
      AND a.mecanico_id = (SELECT MIN(id) FROM mecanico)
)
HAVING COUNT(c.id) > 0 AND COUNT(m.id) > 0;

UPDATE avaliacao
SET cliente_id = 1, mecanico_id = 1, nota = 4,
    comentario = 'Otimo atendimento'
WHERE id = 1;

UPDATE avaliacao
SET cliente_id = 2, mecanico_id = 1, nota = 4,
    comentario = 'Otimo mecanico'
WHERE id = 2;

UPDATE avaliacao
SET cliente_id = 1, mecanico_id = 1, nota = 4,
    comentario = 'Otimo profissional'
WHERE id = 3;

UPDATE avaliacao
SET cliente_id = 4, mecanico_id = 2, nota = 5,
    comentario = 'Excelente atendimento e otimo mecanico'
WHERE id = 4;

UPDATE mecanico m
SET avaliacao_media = (
    SELECT COALESCE(AVG(a.nota), 0)
    FROM avaliacao a
    WHERE a.mecanico_id = m.id
);