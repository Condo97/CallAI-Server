package writesmith.server.database.preparedstatement;

import writesmith.exceptions.PreparedStatementMissingArgumentException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class PreparedStatementBuilder {
    public enum Command {
        SELECT("SELECT"),
        INSERT_INTO("INSERT INTO"),
        UPDATE("UPDATE"),
        DELETE("DELETE");

        public final String string;

        Command(String string) { this.string = string; };
    }

    final String TERMINATOR = ";";
    final String SPACE = " ";
    final String EXPRESSION_OPEN = "(";
    final String EXPRESSION_CLOSE = ")";
    final String PLACEHOLDER = "?";
    final String EQUAL = "=";
    final String SEPARATOR = ", ";

    final String SELECT = Command.SELECT.string;
    final String INSERT_INTO = Command.INSERT_INTO.string;
    final String UPDATE = Command.UPDATE.string;
    final String DELETE = Command.DELETE.string;

    final String ORDER_BY = "ORDER BY";
    final String LIMIT = "LIMIT";
    final String WHERE = "WHERE";
    final String VALUES = "VALUES";
    final String FROM = "FROM";
    final String SET = "SET";
    final String AND = "AND";




    // Common variables
    Connection conn;
    Command command;
    String table;

    List<String> scope;

    // SELECT Variables
//    private ArrayList<String> scopeColumns;

    // WHERE Variables
//    private HashMap<String, String> whereColumns;

    public PreparedStatementBuilder(Connection conn, Command command, String table) {
        this.conn = conn;
        this.command = command;
        this.table = table;

        scope = new ArrayList<>();
    }




    public abstract PreparedStatement build() throws SQLException, PreparedStatementMissingArgumentException;
}
