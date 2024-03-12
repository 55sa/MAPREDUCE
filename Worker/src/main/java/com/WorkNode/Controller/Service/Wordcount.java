package com.WorkNode.Controller.Service;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Component("Wordcount")
public class Wordcount implements Mapper{

    @Override
    public HashMap<String, List<Integer>> mapping(List<String> input) {
        HashMap<String, List<Integer>> wordCounts = new HashMap<>();

        // 遍历输入的每一行
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
                    wordCounts.get(processedWord).add(1);
                } else {
                    // 否则，向map中添加该单词，并将其计数设为1
                    List<Integer> counts = new ArrayList<>();
                    counts.add(1);
                    wordCounts.put(processedWord, counts);
                }
            }
        }

        return wordCounts;

    }
}
