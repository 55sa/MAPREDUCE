package Body;

import java.util.List;

public class IndexReq {
    List<String> files;
    String fn;

    public IndexReq() {
    }

    public IndexReq(List<String> files, String fn) {
        this.files = files;
        this.fn = fn;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    public String getFn() {
        return fn;
    }

    public void setFn(String fn) {
        this.fn = fn;
    }
}
