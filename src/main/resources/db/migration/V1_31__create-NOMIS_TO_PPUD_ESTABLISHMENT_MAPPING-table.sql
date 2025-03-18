DROP TABLE IF EXISTS nomis_to_ppud_establishment_mapping;

CREATE TABLE nomis_to_ppud_establishment_mapping
(
    nomis_agency_id      VARCHAR PRIMARY KEY,
    ppud_establishment   VARCHAR NOT NULL
);

CREATE UNIQUE INDEX idx_nomis_to_ppud_establishment_mapping_PK
    ON nomis_to_ppud_establishment_mapping (nomis_agency_id);