import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class RunCommunitySql {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://127.0.0.1:3306/shouban?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowMultiQueries=true";
        String sql = new String(Files.readAllBytes(Paths.get("F:/work/Vue3/AI_chongwu/ai-sqx-server/sql/community-upgrade-20260625.sql")), "UTF-8");
        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection conn = DriverManager.getConnection(url, "root", "123456");
             Statement stmt = conn.createStatement()) {
            for (String part : sql.split(";")) {
                String piece = part.trim();
                if (piece.isEmpty() || piece.startsWith("--") || piece.equalsIgnoreCase("USE shouban")) {
                    continue;
                }
                stmt.execute(piece);
            }
            System.out.println("SQL migration done");
        }
    }
}
