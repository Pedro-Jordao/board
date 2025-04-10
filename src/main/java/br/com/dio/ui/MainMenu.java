package br.com.dio.ui;

import br.com.dio.persistence.entity.BoardColumnEntity;
import br.com.dio.persistence.entity.BoardColumnKindEnum;
import br.com.dio.persistence.entity.BoardEntity;
import br.com.dio.service.BoardQueryService;
import br.com.dio.service.BoardService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

import static br.com.dio.persistence.config.ConnectionConfig.getConnection;

public class MainMenu  {

    private final Scanner scanner = new Scanner(System.in);

    public void execute()  throws SQLException {
        System.out.println("Bem vindo ao boards");
        var option = -1;
        while(true){
            System.out.println(" 1 Criar Board");
            System.out.println(" 2 Selecionar Board existente");
            System.out.println(" 3 Excluir um board existente");
            System.out.println(" 4 - Sair do boards");
            option = scanner.nextInt();
            switch (option){
                case 1 -> createBoard();

                case 2 -> selectBoard();

                case 3 -> deleteBoard();

                case 4 ->System.exit(0);

                default -> System.out.println("Opção inválida, as opções válidas são: 1 (Criar), 2 (Selecionar), 3 (Excluir), 4 (Sair)");
            }
        }
    }

    private void deleteBoard() throws SQLException {
        System.out.println("Informe o id do board que quer deletar");
        var id= scanner.nextLong();
        try (var connection = getConnection()){
            var service = new BoardService(connection);
            if (service.delete(id)){
                System.out.printf("0 %s foi deletado\n", id);
            }else {
                System.out.printf("O id %s não pôde ser encontrado\n", id);
            }
        }
    }

    private void selectBoard()  throws SQLException {
        System.out.println("Informe o ID do board que procura");
        var id = scanner.nextLong();
        try(var connection = getConnection()){
            var queryService = new BoardQueryService(connection);
            var optional = queryService.findById(id);
            optional.ifPresentOrElse(b -> new BoardMenu(b).execute(), () -> System.out.println("O board procurado não pôde ser encontrado"));

        }
    }

    private void createBoard()  throws SQLException {
        var entity = new BoardEntity();
        System.out.println("Dê um nome ao seu board");
        entity.setName(scanner.next());

        System.out.println("Seu board terá colunas além das 3 padrões? Se sim, informe quantas, senão digite 0");
        var additionalColumns = scanner.nextInt();

        List<BoardColumnEntity> columns = new ArrayList<>();

        System.out.println("Informe o nome da primeira coluna do board");
        var initialColumnName = scanner.next();
        var initialColumn = createColumn(initialColumnName, BoardColumnKindEnum.INITIAL, 0);
        columns.add(initialColumn);

        for (int i = 0; i < additionalColumns; i++){
            System.out.println("Informe a tarefa pendente do board");
            var pendingColumnName = scanner.next();
            var pendingColumn = createColumn(pendingColumnName, BoardColumnKindEnum.PENDING, i + 1);
            columns.add(pendingColumn);
        }
        System.out.println("Informe o nome da última coluna do board");
        var finalColumnName = scanner.next();
        var finalColumn = createColumn(finalColumnName, BoardColumnKindEnum.FINAL, additionalColumns + 1);
        columns.add(finalColumn);

        System.out.println("Informe o nome da coluna de cancelamento do board");
        var cancelColumnName = scanner.next();
        var cancelColumn = createColumn(cancelColumnName, BoardColumnKindEnum.CANCEL, additionalColumns + 2);
        columns.add(cancelColumn);

        entity.setBoardColumns(columns);
        try(var connection = getConnection()){
            var service = new BoardService(connection);
            service.insert(entity);
        }

    }
    private BoardColumnEntity createColumn(final String name, final BoardColumnKindEnum kind, final int order){
        var boardColumn = new BoardColumnEntity();
        boardColumn.setName(name);
        boardColumn.setKind(kind);
        boardColumn.setOrder(order);
        return boardColumn;
    }

}


//TODO:fazer tratativa de erro