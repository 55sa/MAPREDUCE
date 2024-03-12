package Body;

import java.util.List;

public class JobRequest {
    String file;
    int num_map;
    int num_reduce;

    String name;

    List<String> files;

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JobRequest() {
    }

    public JobRequest(String file, int num_map, int num_reduce, String name, List<String> files) {
        this.file = file;
        this.num_map = num_map;
        this.num_reduce = num_reduce;
        this.name = name;
        this.files = files;
    }

    JobRequest(String file, int num_map, int num_reduce, String name){
        this.file=file;
        this.num_map=num_map;
        this.num_reduce=num_reduce;
        this.name=name;

    }

    JobRequest(List<String> files, int num_map, int num_reduce,String name){
        this.files=files;
        this.num_map=num_map;
        this.num_reduce=num_reduce;
        this.name=name;

    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public int getNum_map() {
        return num_map;
    }

    public void setNum_map(int num_map) {
        this.num_map = num_map;
    }

    public int getNum_reduce() {
        return num_reduce;
    }

    public void setNum_reduce(int num_reduce) {
        this.num_reduce = num_reduce;
    }




}
