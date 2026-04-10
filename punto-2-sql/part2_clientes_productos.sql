-- Parte 2 – SQL (base de datos BTG)

SELECT DISTINCT
    c.nombre,
    c.apellidos
FROM cliente AS c
INNER JOIN inscripcion AS i
    ON i.idCliente = c.id
WHERE EXISTS (
    SELECT 1
    FROM disponibilidad AS d0
    WHERE d0.idProducto = i.idProducto
)
AND NOT EXISTS (
    SELECT 1
    FROM disponibilidad AS d
    WHERE d.idProducto = i.idProducto
      AND NOT EXISTS (
          SELECT 1
          FROM visitan AS v
          WHERE v.idCliente = c.id
            AND v.idSucursal = d.idSucursal
      )
);

--Nota: segun el motor de base de datos que se use, se puede usar la concatenacion de strings de la siguiente manera:
-- SELECT DISTINCT c.nombre || ' ' || c.apellidos AS nombre_completo  -- PostgreSQL / Oracle
-- SELECT DISTINCT CONCAT(c.nombre, ' ', c.apellidos) AS nombre_completo  -- MySQL / SQL Server 2012+
