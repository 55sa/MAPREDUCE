package com.master.Service;

import Body.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
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

@Component
public class InvertDispatcher {
    @Autowired
    private RestTemplate restTemplate;
   public ResponseEntity<String> IND(JobRequest job) throws IOException {
       String workerNodeBaseUrl = "http://localhost:9091/invertmap";
       String reducerNodeBaseUrl = "http://localhost:9091/invertredu";
       List<CompletableFuture<HashMap<String, String>>> futures = new ArrayList<>();

    for (int i=0;i<job.getFiles().size();i++){
        List<String> lines = new ArrayList<>();


        Path path = Paths.get("Master/Input/" + job.getFiles().get(i)+".txt");

        lines = Files.readAllLines(path);
        lines.removeIf(String::isEmpty);
        CompletableFuture<HashMap<String, String>> future=sendTaskAsync(workerNodeBaseUrl,new IndexReq(lines,job.getFiles().get(i)));

        futures.add(future);

    }
       CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        HashMap<String,List<String>> shuffle=new HashMap<>();
       for (CompletableFuture<HashMap<String, String>> future : futures) {
           try {
               HashMap<String, String> result = future.get(); // 获取异步操作的结果
               // 遍历结果中的每个条目
               for (Map.Entry<String, String> entry : result.entrySet()) {
                   String key = entry.getKey();
                   String value = entry.getValue();

                   // 如果key已经存在于shuffle中，则添加value到对应的list
                   shuffle.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
               }
           } catch (InterruptedException | ExecutionException e) {
               e.printStackTrace(); // 实际应用中应进行适当的异常处理
           }
       }

       int totalEntries = shuffle.size();
       int entriesPerReducer = (int) Math.ceil((double) totalEntries / job.getNum_reduce());

       // 将combinedResults分割成多个HashMap，每个对应一个Reducer
       List<HashMap<String, List<String>>> slices = new ArrayList<>();
       List<String> keys = new ArrayList<>(shuffle.keySet());

       for (int i = 0; i < totalEntries; i += entriesPerReducer) {
           HashMap<String, List<String>> slice = new HashMap<>();
           int end = Math.min(i + entriesPerReducer, totalEntries);
           for (String key : keys.subList(i, end)) {
               slice.put(key, shuffle.get(key));
           }
           slices.add(slice);
       }

       List<CompletableFuture<HashMap<String, String>>> reduceFutures = new ArrayList<>();

       // 遍历slices，异步发送给reducer，并收集Future
       for (HashMap<String, List<String>> slice : slices) {
           // 假设map类封装了HashMap，并且提供了getSection方法来获取数据
           // 注意：这里的map li = new map(slice); 语句假设你有一个适合的构造函数或方法设置slice
           // 以下代码需要根据你map类的实际实现进行调整
           inverjob li = new inverjob(slice);
           // 假设setSection是设置数据的方法
           CompletableFuture<HashMap<String, String>> future = sendReduceTaskAsync(reducerNodeBaseUrl, li);
           reduceFutures.add(future);
       }


       // 等待所有归约任务完成
       CompletableFuture.allOf(reduceFutures.toArray(new CompletableFuture[0])).join();

       // 收集归约结果并合并
       HashMap<String, String> finalReducedResults = new HashMap<>();
       for (CompletableFuture<HashMap<String, String>> feat : reduceFutures) {
           HashMap<String, String> reducedResult = null; // 注意处理可能的异常
           try {
               reducedResult = feat.get();
           } catch (InterruptedException e) {
               throw new RuntimeException(e);
           } catch (ExecutionException e) {
               throw new RuntimeException(e);
           }
           finalReducedResults.putAll(reducedResult);
       }
       Path outputDir = Paths.get("Master/Invert-Output");
       String name = String.join("-", job.getFiles());
       Path outputPath = outputDir.resolve(name+"-summary"+".txt");
       List<String> outputLines = finalReducedResults.entrySet().stream()
               .map(e -> e.getKey() + ": " + e.getValue())
               .collect(Collectors.toList());

       Files.write(outputPath, outputLines, StandardCharsets.UTF_8,
               StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);



       return ResponseEntity.ok("All task complete!");
   }

   @Async
    public CompletableFuture<HashMap<String, String>> sendTaskAsync(String workerNodeUrl, IndexReq taskRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<IndexReq> request = new HttpEntity<>(taskRequest, headers);

        ResponseEntity<HashMap<String, String>> response = restTemplate.exchange(
                workerNodeUrl,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<HashMap<String, String>>() {});
        return CompletableFuture.completedFuture(response.getBody());
    }

    @Async
    public CompletableFuture<HashMap<String, String>> sendReduceTaskAsync(String reducerNodeUrl, inverjob li) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // 假设li是一个包含HashMap<String, List<Integer>>的对象，并且map类有一个getSection()方法
        HttpEntity<inverjob> request = new HttpEntity<>(li, headers);

        ResponseEntity<HashMap<String, String>> response = restTemplate.exchange(
                reducerNodeUrl,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<HashMap<String, String>>() {});

        return CompletableFuture.completedFuture(response.getBody());
    }


}
