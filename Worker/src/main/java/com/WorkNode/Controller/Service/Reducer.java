package com.WorkNode.Controller.Service;

import java.util.HashMap;
import java.util.List;

public interface Reducer {

    public HashMap<String,Integer> reducing(HashMap<String, List<Integer>> input);
}
