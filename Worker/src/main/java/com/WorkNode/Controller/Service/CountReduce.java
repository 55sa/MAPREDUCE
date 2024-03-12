package com.WorkNode.Controller.Service;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Component("ctr")
public class CountReduce implements Reducer{
    public HashMap<String,Integer> reducing(HashMap<String, List<Integer>> input){
        HashMap<String, Integer> result = new HashMap<>();
        for (Map.Entry<String, List<Integer>> entry : input.entrySet()) {
            String word = entry.getKey();
            List<Integer> counts = entry.getValue();

            // 计算当前单词的总计数
            int sum = counts.stream().mapToInt(Integer::intValue).sum();

            // 将单词和其总计数存储在结果HashMap中
            result.put(word, sum);
        }


        return result;


    }
}
