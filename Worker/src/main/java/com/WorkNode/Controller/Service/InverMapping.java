package com.WorkNode.Controller.Service;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InverMapping{
    public HashMap<String,String> mapping(List<String> input,String fn){
        HashMap<String,Integer> wordCounts=new HashMap<>();

        for (String line : input) {
            // 使用正则表达式分割每一行成单词，同时去除标点符号和引号
            // 包括空格、常见标点、单引号、双引号
            List<String> words = Arrays.asList(line.split("\\s+|[,.!?;:\"“”'()\\[\\]{}<>-]+"))
                    .stream()
                    .filter(word -> !word.isEmpty()) // 过滤空字符串
                    .collect(Collectors.toList());

            // 遍历每个单词
            for (String word : words) {
                // 进行小写转换或其他预处理
                String processedWord = word.toLowerCase(); // 可以在这里添加更多的文本预处理
                // 如果单词已存在于map中，增加其计数
                if (wordCounts.containsKey(processedWord)) {
                    wordCounts.put(processedWord,wordCounts.get(processedWord)+1);
                } else {
                    // 否则，向map中添加该单词，并将其计数设为1

                    wordCounts.put(processedWord, 1);
                }
            }
        }

        HashMap<String,String> result=new HashMap<>();


        for (Map.Entry<String, Integer> entry : wordCounts.entrySet()) {
            String key = entry.getKey();
            Integer count = entry.getValue();
            // 将Integer计数转换为fn+Integer的格式
            String formattedCount = fn +"-"+ count.toString();
            // 存储到结果HashMap中
            result.put(key, formattedCount);
        }

        return result;


    }

}
