package Body;

import java.util.List;

public class TaskRequest {
    List<String> lines;

    public TaskRequest() {

    }
    public TaskRequest(List<String> lines) {
        this.lines = lines;
    }

    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = lines;
    }
}
