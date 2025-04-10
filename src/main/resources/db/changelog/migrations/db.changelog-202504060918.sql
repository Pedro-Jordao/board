--liquibase formatted sql
--changeset pedro:202504060918
--comment: set unblock_reason nullable (TODO : lembrar motivo da mudança para entrevista)

ALTER TABLE BLOCKS MODIFY COLUMN unblock_reason varchar(255) NULL;
--rollback ALTER TABLE BLOCKS MODIFY COLUMN unblock_reason varchar(255) NOT NULL;
