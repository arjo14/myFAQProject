import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class URLConnectionReader {

    public static void main(String[] args) throws Exception {

        List<QuestionsDto> questionsAndAnswers = getQuestionsAndAnswers();

        Class.forName("org.h2.Driver");
        Connection conn = DriverManager.getConnection("jdbc:h2:~/FAQ", "sa", "");

        insertQuestions(conn, questionsAndAnswers);

        System.out.println("");
        conn.close();
    }

    private static void printQuestions(Connection conn, List<QuestionsDto> questionsAndAnswers) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("SELECT* FROM QUESTIONS");
        ResultSet rs = statement.executeQuery();
        while (rs.next()) {
            System.out.println(rs.getInt("ID")+"  "+ rs.getInt("topic_id")+"  "+rs.getString("question")+"  "+rs.getString("answer"));
        }
    }

    private static Map<Integer, String> getTopicNames(Connection conn) throws SQLException {
        Map<Integer, String> map = new HashMap<Integer, String>();
        PreparedStatement statement = conn.prepareStatement("SELECT* FROM TOPIC");
        ResultSet rs = statement.executeQuery();
        while (rs.next()) {
            map.put(rs.getInt("ID"), rs.getString("topic_name"));
        }
        return map;
    }

    private static void insertQuestions(Connection conn, List<QuestionsDto> questionsAndAnswers) throws SQLException {
        PreparedStatement insertPreparedStatement;
        String InsertQuery = "INSERT INTO QUESTIONS (topic_id,question,answer) VALUES (?,?,?)";
        insertPreparedStatement = conn.prepareStatement(InsertQuery);
        for (int i = 0, size = questionsAndAnswers.size(); i < size; i++) {
            Map<String, StringBuilder> map = questionsAndAnswers.get(i).getMap();
            Iterator<String> it = map.keySet().iterator();
            for (int j = 0, mapSize = map.size(); j < mapSize; j++) {
                String question=it.next();
                insertPreparedStatement.setInt(1, i + 1);
                insertPreparedStatement.setString(2, question);
                insertPreparedStatement.setString(3, String.valueOf(map.get(question)));
                insertPreparedStatement.executeUpdate();
            }
        }
        insertPreparedStatement.close();
    }

    private static void insertTopics(Connection conn, List<QuestionsDto> questionsAndAnswers) throws SQLException {
        PreparedStatement insertPreparedStatement;
        String InsertQuery = "INSERT INTO TOPIC (id, topic_name) VALUES (?,?)";
        insertPreparedStatement = conn.prepareStatement(InsertQuery);
        for (int i = 0; i < questionsAndAnswers.size(); i++) {
            insertPreparedStatement.setInt(1, i + 21);
            insertPreparedStatement.setString(2, questionsAndAnswers.get(i).getTopicName());
            insertPreparedStatement.executeUpdate();
        }
        insertPreparedStatement.close();
    }

    private static List<QuestionsDto> getQuestionsAndAnswers() throws IOException {
        Document doc = Jsoup.connect("https://telegram.org/faq").get();
        Elements questionElements = doc.select("#dev_page_content  h3, h4, h3 ~ p");

        String question = null;
        String topicName = null;
        StringBuilder answer = new StringBuilder();
        List<QuestionsDto> list = new ArrayList<QuestionsDto>();
        Map<String, StringBuilder> map = new HashMap<String, StringBuilder>();

        for (int i = 0, size = questionElements.size(); i < size; i++) {
            if ("Troubleshooting".equals(questionElements.get(i).text())) {
                map.put(question, answer);
                list.add(new QuestionsDto(topicName, map));
                break;
            }
            if (questionElements.get(i).tagName().equals("h3")) {
                if (topicName != null) {
                    list.add(new QuestionsDto(topicName, map));
                    map = new HashMap<String, StringBuilder>();
                    question = null;
                    answer = new StringBuilder();
                }
                topicName = questionElements.get(i).text();

            } else if (questionElements.get(i).tagName().equals("h4")) {
                if (question != null) {
                    map.put(question, answer);
                    answer = new StringBuilder();
                }
                question = questionElements.get(i).text().substring(3);
            } else {
                answer.append(questionElements.get(i).text());
            }
        }
        return list;
    }
}
