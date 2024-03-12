package Body;

import java.util.HashMap;
import java.util.List;

public class map {
    HashMap<String, List<Integer>> section;

    public map() {

    }
    public map(HashMap<String, List<Integer>> section) {
        this.section = section;
    }

    public HashMap<String, List<Integer>> getSection() {
        return section;
    }

    public void setSection(HashMap<String, List<Integer>> section) {
        this.section = section;
    }
}
