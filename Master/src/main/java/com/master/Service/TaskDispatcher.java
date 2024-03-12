package com.master.Service;

import Body.JobRequest;

import Body.TaskRequest;
import Body.map;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service("TK")
public class TaskDispatcher implements Servicelayer{
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ResourceLoader resourceLoader;


    public ResponseEntity<String> dispatchTasks(JobRequest jobRequest)throws IOException {
        String workerNodeBaseUrl = "http://localhost:9091/process-task";
        String reducerNodeBaseUrl = "http://localhost:9091/reduce-task";

        // 加载文件
        Resource resource = resourceLoader.getResource("Input/" + jobRequest.getFile()+".txt");
        List<String> lines = new ArrayList<>();


        Path path = Paths.get("Master/Input/" + jobRequest.getFile()+".txt");

        lines = Files.readAllLines(path);
        lines.remove("");
        ResponseEntity<String> res = null;
        // 确定每个Mapper任务应处理的行数
        int totalLines = lines.size();

        int linesPerMap = (int) Math.ceil((double) totalLines / jobRequest.getNum_map());


        List<CompletableFuture<HashMap<String, List<Integer>>>> futures = new ArrayList<>();

        for (int i = 0; i < jobRequest.getNum_map(); i++) {
            int start = i * linesPerMap;
            int end = Math.min(start + linesPerMap, totalLines);

            List<String> taskLines = lines.subList(start, end);

            CompletableFuture<HashMap<String, List<Integer>>> future = sendTaskAsync(workerNodeBaseUrl, new TaskRequest(taskLines));
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<HashMap<String, List<Integer>>> results = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());




        HashMap<String, List<Integer>> combinedResults = new HashMap<>();

// Iterate over each result HashMap
        for (HashMap<String, List<Integer>> result : results) {
            // Iterate over each entry in the current HashMap
            for (Map.Entry<String, List<Integer>> entry : result.entrySet()) {
                String word = entry.getKey();
                List<Integer> counts = entry.getValue();

                // If the word is already in the combinedResults, append the counts
                if (combinedResults.containsKey(word)) {
                    combinedResults.get(word).addAll(counts);
                } else {
                    // If the word is not in the combinedResults, put the new word with its counts
                    combinedResults.put(word, new ArrayList<>(counts));
                }
            }
        }





        // 计算每个Reducer应处理的条目数量
        int totalEntries = combinedResults.size();
        int entriesPerReducer = (int) Math.ceil((double) totalEntries / jobRequest.getNum_reduce());

        // 将combinedResults分割成多个HashMap，每个对应一个Reducer
        List<HashMap<String, List<Integer>>> slices = new ArrayList<>();
        List<String> keys = new ArrayList<>(combinedResults.keySet());

        for (int i = 0; i < totalEntries; i += entriesPerReducer) {
            HashMap<String, List<Integer>> slice = new HashMap<>();
            int end = Math.min(i + entriesPerReducer, totalEntries);
            for (String key : keys.subList(i, end)) {
                slice.put(key, combinedResults.get(key));
            }
            slices.add(slice);
        }

        List<CompletableFuture<HashMap<String, Integer>>> reduceFutures = new ArrayList<>();

        // 遍历slices，异步发送给reducer，并收集Future
        for (HashMap<String, List<Integer>> slice : slices) {
            // 假设map类封装了HashMap，并且提供了getSection方法来获取数据
            // 注意：这里的map li = new map(slice); 语句假设你有一个适合的构造函数或方法设置slice
            // 以下代码需要根据你map类的实际实现进行调整
            map li = new map(slice);
            // 假设setSection是设置数据的方法
            CompletableFuture<HashMap<String, Integer>> future = sendReduceTaskAsync(reducerNodeBaseUrl, li);
            reduceFutures.add(future);
        }


        // 等待所有归约任务完成
        CompletableFuture.allOf(reduceFutures.toArray(new CompletableFuture[0])).join();



        // 收集归约结果并合并
        HashMap<String, Integer> finalReducedResults = new HashMap<>();
        for (CompletableFuture<HashMap<String, Integer>> feat : reduceFutures) {
            HashMap<String, Integer> reducedResult = null; // 注意处理可能的异常
            try {
                reducedResult = feat.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
            finalReducedResults.putAll(reducedResult);
        }
        Path outputDir = Paths.get("Master/Output");
        Path outputPath = outputDir.resolve(jobRequest.getFile()+"-summary"+".txt");
        List<String> outputLines = finalReducedResults.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.toList());

        Files.write(outputPath, outputLines, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);




        return ResponseEntity.ok("All tasks dispatched and results written to file successfully.");
    }




    @Async
    public CompletableFuture<HashMap<String, List<Integer>>> sendTaskAsync(String workerNodeUrl, TaskRequest taskRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TaskRequest> request = new HttpEntity<>(taskRequest, headers);

        ResponseEntity<HashMap<String, List<Integer>>> response = restTemplate.exchange(
                workerNodeUrl,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<HashMap<String, List<Integer>>>() {});
        return CompletableFuture.completedFuture(response.getBody());
    }

    @Async
    public CompletableFuture<HashMap<String, Integer>> sendReduceTaskAsync(String reducerNodeUrl, map li) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // 假设li是一个包含HashMap<String, List<Integer>>的对象，并且map类有一个getSection()方法
        HttpEntity<map> request = new HttpEntity<>(li, headers);

        ResponseEntity<HashMap<String, Integer>> response = restTemplate.exchange(
                reducerNodeUrl,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<HashMap<String, Integer>>() {});

        return CompletableFuture.completedFuture(response.getBody());
    }
}


