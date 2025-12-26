-- =================================================================
-- 1. LIMPIEZA TOTAL (Borrar todo y empezar de cero)
-- =================================================================
DROP TABLE IF EXISTS task CASCADE;
DROP TABLE IF EXISTS sector CASCADE;
DROP TABLE IF EXISTS db_user CASCADE;

-- Asegurar extensión PostGIS
CREATE EXTENSION IF NOT EXISTS postgis;

-- =================================================================
-- 2. CREACIÓN DE TABLAS
-- =================================================================

-- Tabla de usuarios
CREATE TABLE db_user (
    user_id      SERIAL PRIMARY KEY,
    username     VARCHAR(50)  NOT NULL UNIQUE,
    firstname    VARCHAR(50)  NOT NULL,
    lastname     VARCHAR(50)  NOT NULL,
    password     VARCHAR(255) NOT NULL,
    role         VARCHAR(20)  NOT NULL,
    address      VARCHAR(255),
    latitude     DOUBLE PRECISION,
    longitude    DOUBLE PRECISION,
    location     geometry(Point, 4326)
);

-- Tabla de sectores
CREATE TABLE sector (
    id        SERIAL PRIMARY KEY,
    name      VARCHAR(100) NOT NULL UNIQUE,
    location  geometry(Point, 4326) NOT NULL
);

-- Tabla de tareas
CREATE TABLE task (
    id          SERIAL PRIMARY KEY,
    title       VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    due_date    DATE NOT NULL,
    finished    BOOLEAN NOT NULL DEFAULT FALSE,
    important   BOOLEAN NOT NULL DEFAULT FALSE,
    user_id     INTEGER NOT NULL REFERENCES db_user(user_id),
    sector_id   INTEGER REFERENCES sector(id)
);

-- Índices espaciales (Vital para el rendimiento de ST_Distance)
CREATE INDEX idx_user_location   ON db_user USING GIST (location);
CREATE INDEX idx_sector_location ON sector  USING GIST (location);

-- =================================================================
-- 3. INSERCIÓN DE DATOS ESTRATÉGICOS
-- =================================================================

DO $$ 
DECLARE 
    v_user_id INTEGER;
    v_sec_cerca INTEGER;   -- A < 1km
    v_sec_medio INTEGER;   -- A ~ 3km
    v_sec_lejos INTEGER;   -- A > 10km
BEGIN 
    -- A. Insertar Usuario ADMIN (Ubicación: Santiago Centro - Metro Moneda)
    -- Password es '123' (hash bcrypt)
    INSERT INTO db_user (username, firstname, lastname, password, role, address, latitude, longitude, location)
    VALUES (
        'admin', 'Admin', 'Principal', 
        '$2a$10$DomUxjJ/C3.jVn.u4.XZX.y.0/0.0/0.0/0.0/0.0/0.0/0.0/0.0', 
        'ADMIN', 
        'Santiago Centro, Moneda', 
        -33.4429, -70.6539, 
        ST_SetSRID(ST_MakePoint(-70.6539, -33.4429), 4326)
    ) RETURNING user_id INTO v_user_id;

    -- B. Insertar Sectores Relativos al Usuario
    
    -- 1. Sector "Centro Cívico" (A 200 metros -> Entra en radio 2km)
    INSERT INTO sector (name, location) 
    VALUES ('Centro Cívico', ST_SetSRID(ST_MakePoint(-70.6530, -33.4450), 4326))
    RETURNING id INTO v_sec_cerca;

    -- 2. Sector "Providencia" (A 3.5 km -> Entra en radio 5km, fuera de 2km)
    INSERT INTO sector (name, location) 
    VALUES ('Providencia', ST_SetSRID(ST_MakePoint(-70.6300, -33.4300), 4326))
    RETURNING id INTO v_sec_medio;

    -- 3. Sector "Aeropuerto" (A 15 km -> Fuera de todo radio)
    INSERT INTO sector (name, location) 
    VALUES ('Aeropuerto', ST_SetSRID(ST_MakePoint(-70.7900, -33.3900), 4326))
    RETURNING id INTO v_sec_lejos;

    -- C. Insertar Tareas para generar las Estadísticas

    -- >> Métrica: Sector Top en 2km (Haremos que gane "Centro Cívico")
    INSERT INTO task (title, description, due_date, finished, important, user_id, sector_id) VALUES
    ('Limpieza Plaza Constitución', 'Aseo profundo', '2025-10-01', TRUE, FALSE, v_user_id, v_sec_cerca),
    ('Reparación Luminaria', 'Cambio de focos LED', '2025-10-02', TRUE, TRUE, v_user_id, v_sec_cerca),
    ('Pintura de Bancas', 'Mantenimiento urbano', '2025-10-03', TRUE, FALSE, v_user_id, v_sec_cerca);

    -- >> Métrica: Sector Top en 5km (Se suman las de arriba + Providencia)
    INSERT INTO task (title, description, due_date, finished, important, user_id, sector_id) VALUES
    ('Poda Parque Bustamante', 'Mantención áreas verdes', '2025-10-05', TRUE, FALSE, v_user_id, v_sec_medio),
    ('Fiscalización Restaurantes', 'Revisión sanitaria', '2025-10-06', TRUE, FALSE, v_user_id, v_sec_medio);

    -- >> Métrica: Tarea Pendiente más Cercana (Debe ser esta en Centro Cívico)
    INSERT INTO task (title, description, due_date, finished, important, user_id, sector_id) VALUES
    ('URGENTE: Bache en la Alameda', 'Reparación de pavimento calle principal', '2025-11-28', FALSE, TRUE, v_user_id, v_sec_cerca);

    -- >> Métrica: Concentración de Pendientes (Para estadística global)
    INSERT INTO task (title, description, due_date, finished, important, user_id, sector_id) VALUES
    ('Revisión Pista Aterrizaje', 'Revisión técnica', '2025-12-01', FALSE, FALSE, v_user_id, v_sec_lejos);

END $$;




INSERT INTO task (
    title,
    description,
    due_date,
    finished,
    important,
    user_id,
    sector_id
)
VALUES
(
    'Reparar bache en calle 1',
    'Reparación de bache en intersección principal.',
    '2025-11-25',
    FALSE,
    TRUE,
    (SELECT user_id FROM db_user WHERE username = 'admin'),
    (SELECT id FROM sector WHERE name = 'Calles')
),
(
    'Inspección edificio municipal',
    'Revisión de estructura y seguridad del edificio municipal.',
    '2025-11-24',
    FALSE,
    FALSE,
    (SELECT user_id FROM db_user WHERE username = 'admin'),
    (SELECT id FROM sector WHERE name = 'Construcción')
),
(
    'Revisión semáforos Av. Alameda',
    'Verificar sincronización y funcionamiento de los semáforos en Av. Alameda.',
    '2025-11-22',
    TRUE,
    TRUE,
    (SELECT user_id FROM db_user WHERE username = 'admin'),
    (SELECT id FROM sector WHERE name = 'Reparación de semáforos')
),
(
    'Pintar señalización vial',
    'Repintado de pasos de cebra y líneas divisorias.',
    '2025-11-28',
    FALSE,
    FALSE,
    (SELECT user_id FROM db_user WHERE username = 'admin'),
    (SELECT id FROM sector WHERE name = 'Calles')
),
(
    'Revisión de andamios en obra',
    'Control de seguridad en los andamios de la obra principal.',
    '2025-11-20',
    TRUE,
    FALSE,
    (SELECT user_id FROM db_user WHERE username = 'admin'),
    (SELECT id FROM sector WHERE name = 'Construcción')
);




-- 1. Insertar Sectores estratégicos relativos a la posición del admin (-33.4489, -70.6693)

-- Sector A: "Barrio Cívico" (A unos 500 metros - Dentro del radio de 2km)
INSERT INTO sector (name, location)
VALUES (
    'Barrio Cívico',
    ST_SetSRID(ST_MakePoint(-70.6650, -33.4450), 4326)
) ON CONFLICT (name) DO NOTHING;

-- Sector B: "Barrio Bellavista" (A unos 2.5 km - Fuera de 2km, dentro de 5km)
INSERT INTO sector (name, location)
VALUES (
    'Barrio Bellavista',
    ST_SetSRID(ST_MakePoint(-70.6350, -33.4350), 4326)
) ON CONFLICT (name) DO NOTHING;

-- Sector C: "Las Condes" (A unos 8 km - Fuera de 5km)
INSERT INTO sector (name, location)
VALUES (
    'Las Condes Lejano',
    ST_SetSRID(ST_MakePoint(-70.5800, -33.4100), 4326)
) ON CONFLICT (name) DO NOTHING;


-- 2. Insertar Tareas para el usuario 'admin'

-- TAREAS COMPLETADAS EN EL RADIO DE 2KM (Para activar métrica de 2km y promedio distancia)
INSERT INTO task (title, description, due_date, finished, important, user_id, sector_id)
VALUES 
(
    'Limpieza Plaza Cívica', 
    'Barrido y limpieza general.', 
    '2025-10-01', 
    TRUE, -- Completada
    FALSE, 
    (SELECT user_id FROM db_user WHERE username = 'admin'),
    (SELECT id FROM sector WHERE name = 'Barrio Cívico')
),
(
    'Instalación luminaria centro', 
    'Cambio de focos LED.', 
    '2025-10-05', 
    TRUE, -- Completada
    TRUE, 
    (SELECT user_id FROM db_user WHERE username = 'admin'),
    (SELECT id FROM sector WHERE name = 'Barrio Cívico')
);

-- TAREAS COMPLETADAS EN EL RADIO DE 5KM (Para activar métrica de 5km)
INSERT INTO task (title, description, due_date, finished, important, user_id, sector_id)
VALUES 
(
    'Poda árboles Bellavista', 
    'Mantención de áreas verdes.', 
    '2025-10-10', 
    TRUE, -- Completada
    FALSE, 
    (SELECT user_id FROM db_user WHERE username = 'admin'),
    (SELECT id FROM sector WHERE name = 'Barrio Bellavista')
);

-- TAREA PENDIENTE MUY CERCANA (Para activar "Tarea más cercana pendiente")
INSERT INTO task (title, description, due_date, finished, important, user_id, sector_id)
VALUES 
(
    'Reparación urgente vereda', 
    'Vereda rota frente a edificio gubernamental.', 
    '2025-11-30', 
    FALSE, -- Pendiente
    TRUE, 
    (SELECT user_id FROM db_user WHERE username = 'admin'),
    (SELECT id FROM sector WHERE name = 'Barrio Cívico') -- Está muy cerca
);

-- TAREA LEJANA (Para variar el promedio)
INSERT INTO task (title, description, due_date, finished, important, user_id, sector_id)
VALUES 
(
    'Inspección puente', 
    'Revisión estructural.', 
    '2025-12-01', 
    FALSE, -- Pendiente
    FALSE, 
    (SELECT user_id FROM db_user WHERE username = 'admin'),
    (SELECT id FROM sector WHERE name = 'Las Condes Lejano')
);


UPDATE db_user 
SET 
    firstname = 'Admin',
    lastname = 'System',
    password = '$2b$12$ukfwbI0mWBrq5flw28KGaO9D/w6tnooZmjanBxvhLEn8AO6hyNpTi',
    role = 'ADMIN',
    address = 'Santiago Centro',
    latitude = -33.4489,
    longitude = -70.6693,
    location = ST_SetSRID(ST_MakePoint(-70.6693, -33.4489), 4326)
WHERE username = 'admin';




DO $$ 
DECLARE 
    v_user_id INTEGER;
    v_sector_1 INTEGER;
    v_sector_2 INTEGER;
BEGIN 
    
    SELECT user_id INTO v_user_id FROM db_user WHERE username = 'pablox';

   
    IF v_user_id IS NULL THEN
        RAISE NOTICE 'El usuario pablox no existe. Créalo primero en el registro.';
    ELSE
        
        
        SELECT id INTO v_sector_1 FROM sector LIMIT 1; 
        SELECT id INTO v_sector_2 FROM sector ORDER BY id DESC LIMIT 1;

        
        INSERT INTO task (title, description, due_date, finished, important, user_id, sector_id)
        VALUES 
        ('Revisión de Cables', 'Revisar cableado en la zona norte', '2025-11-30', FALSE, TRUE, v_user_id, v_sector_1),
        ('Reparar Vereda Casa', 'Arreglar entrada de vehículo', '2025-12-05', FALSE, FALSE, v_user_id, v_sector_2);

       
        INSERT INTO task (title, description, due_date, finished, important, user_id, sector_id)
        VALUES 
        ('Comprar Materiales', 'Cemento y arena', '2025-10-15', TRUE, FALSE, v_user_id, v_sector_1);
        
        RAISE NOTICE 'Tareas insertadas correctamente para el usuario: pablox (ID: %)', v_user_id;
    END IF;
END $$;