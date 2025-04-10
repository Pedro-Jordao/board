package br.com.dio.service;

import br.com.dio.dto.BoardColumnInfoDTO;
import br.com.dio.dto.CardDetailsDTO;
import br.com.dio.exception.CardBlockedException;
import br.com.dio.exception.CardFinishedException;
import br.com.dio.exception.EntityNotFoundException;
import br.com.dio.persistence.dao.BlockDAO;
import br.com.dio.persistence.dao.CardDAO;
import br.com.dio.persistence.entity.CardEntity;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static br.com.dio.persistence.entity.BoardColumnKindEnum.CANCEL;
import static br.com.dio.persistence.entity.BoardColumnKindEnum.FINAL;

@AllArgsConstructor
public class CardService {

    private final Connection connection;

    public CardEntity insert(final CardEntity entity) throws SQLException {
        try {

            var dao = new CardDAO(connection);
            dao.insert(entity);
            connection.commit();
            return entity;
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        }
    }

    public void moveToNextColumn(final Long cardId, final List<BoardColumnInfoDTO> boardColumnsInfo) throws SQLException {

        try {
            var dao = new CardDAO(connection);
            var optional = dao.findById(cardId);
            var dto = optional.orElseThrow(
                    () -> new EntityNotFoundException("O card de id %s não foi encontrado".formatted(cardId)
                    )
            );
            if (dto.blocked()) {
                var messageToExcept = "O card referido está bloqueado. Se esse realmente é o card que procura, é necessário desbloqueá-lo. Verifique se realmente procura pelo card de id %s?".formatted(cardId);
                throw new CardBlockedException(messageToExcept);
            }
            var currentColumn = boardColumnsInfo.stream().filter(bc -> bc.id().equals(dto.columnId())).findFirst().orElseThrow(() -> new IllegalStateException("O card informado não existe ou pertence a outro board"));
            if (currentColumn.kind().equals(FINAL)) {
                throw new CardFinishedException("O card já está finalizado");
            }
            var nextColumn = boardColumnsInfo.stream().filter(bc -> bc.order() == currentColumn.order() + 1)
                    .findFirst().orElseThrow(() -> new IllegalStateException("O card referido está cancelado"));
            dao.moveToColumn(nextColumn.id(), cardId);
            connection.commit();

        } catch (SQLException ex) {
            connection.rollback();
            throw ex;

        }
    }

    public void cancel(final Long cardId, final Long cancelColumnId, final List<BoardColumnInfoDTO> boardColumnsInfo) throws SQLException{
        try {
            var dao = new CardDAO(connection);
            var optional = dao.findById(cardId);
            var dto = optional.orElseThrow(
                    () -> new EntityNotFoundException("O card de id %s não foi encontrado".formatted(cardId)
                    )
            );
            if (dto.blocked()) {
                var messageToExcept = "O card referido está bloqueado. Se esse realmente é o card que procura, é necessário desbloqueá-lo. Verifique se realmente procura pelo card de id %s?".formatted(cardId);
                throw new CardBlockedException(messageToExcept);
            }
            var currentColumn = boardColumnsInfo.stream().filter(bc -> bc.id().equals(dto.columnId())).findFirst().orElseThrow(() -> new IllegalStateException("O card informado pertence a outro board"));
            if (currentColumn.kind().equals(FINAL)) {
                throw new CardFinishedException("O card já está finalizado");
            }
            boardColumnsInfo.stream().filter(bc -> bc.order() == currentColumn.order() + 1)
                    .findFirst().orElseThrow(() -> new IllegalStateException("O card referido está cancelado"));
            dao.moveToColumn(cancelColumnId, cardId);
            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;

        }
    }

    public void block(final Long id, final String reason, final List<BoardColumnInfoDTO> boardColumnsInfo) throws SQLException{
        try{
            var dao = new CardDAO(connection);

            var optional = dao.findById(id);
            var dto = optional.orElseThrow(
                    () -> new EntityNotFoundException("O card de id %s não foi encontrado".formatted(id)
                    )
            );
            if (dto.blocked()) {
                var messageToExcept = "O card referido [%s] JÁ está bloqueado".formatted(id);
                throw new CardBlockedException(messageToExcept);
            }
            var currentColumn = boardColumnsInfo.stream().filter(bc -> bc.id().equals(dto.columnId())).findFirst().orElseThrow()
                    ;
            if(currentColumn.kind().equals(FINAL) || currentColumn.kind().equals(CANCEL) ){
                throw new IllegalStateException("O card está em uma coluna do tipo %s e não pode ser bloqueado".formatted(currentColumn.kind()));
            }
            var blockDAO = new BlockDAO(connection);
            blockDAO.block(reason, id);
            connection.commit();
        }catch (SQLException ex){
            connection.rollback();
            throw ex;

        }
    }
    public void unblock(final Long id, final String reason) throws SQLException{
        try{
            var dao = new CardDAO(connection);

            var optional = dao.findById(id);
            var dto = optional.orElseThrow(
                    () -> new EntityNotFoundException("O card de id %s não foi encontrado".formatted(id)
                    )
            );
            if (!dto.blocked()) {
                var messageToExcept = "O card referido [%s] NÃO está bloqueado".formatted(id);
                throw new CardBlockedException(messageToExcept);
            }
            var blockDAO = new BlockDAO(connection);
            blockDAO.unblock(reason, id);
            connection.commit();
        }catch (SQLException ex){
            connection.rollback();
            throw ex;
        }
    }
}
