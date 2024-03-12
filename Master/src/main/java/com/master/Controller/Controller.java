package com.master.Controller;

import Body.JobRequest;
import com.master.Service.InvertDispatcher;
import com.master.Service.Servicelayer;
import com.master.Service.TaskDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class Controller {
    @Autowired
    @Qualifier("TK")
    private Servicelayer taskDispatcher;
    @Autowired
    InvertDispatcher invertDispatcher;
    @GetMapping
  public   ResponseEntity<String>  done(){
        return ResponseEntity.ok("IMOK");
    }

    @PostMapping("/start")
    public ResponseEntity<String> submitJob(@RequestBody JobRequest jobRequest) throws IOException {
        if(jobRequest.getName().equals("word-count"))
        {
        return taskDispatcher.dispatchTasks(jobRequest);}
        else if(jobRequest.getName().equals("invert-index")){
            return invertDispatcher.IND(jobRequest);
        }
        else {
            return null;
        }
    }


}
