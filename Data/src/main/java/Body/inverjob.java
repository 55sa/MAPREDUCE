package Body;

import java.util.HashMap;
import java.util.List;

public class inverjob {
    HashMap<String, List<String>> res;

    public HashMap<String, List<String>> getRes() {
        return res;
    }

    public void setRes(HashMap<String, List<String>> res) {
        this.res = res;
    }

    public inverjob() {
    }

    public inverjob(HashMap<String, List<String>> res) {
        this.res = res;
    }
}
