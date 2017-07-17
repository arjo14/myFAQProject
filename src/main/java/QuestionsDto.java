import java.util.Map;

/**
 * author John Smith .
 */
public class QuestionsDto {
    private String topicName;
    private Map<String,StringBuilder> map;

    public QuestionsDto(String topicName, Map<String, StringBuilder> map) {
        this.topicName = topicName;
        this.map = map;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public Map<String, StringBuilder> getMap() {
        return map;
    }

    public void setMap(Map<String, StringBuilder> map) {
        this.map = map;
    }
}
