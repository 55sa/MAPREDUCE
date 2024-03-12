package com.WorkNode.Controller;

import Body.*;
import com.WorkNode.Controller.Service.InverMapping;
import com.WorkNode.Controller.Service.InverRedu;
import com.WorkNode.Controller.Service.Mapper;
import com.WorkNode.Controller.Service.Reducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

@RestController
public class WorkController {

    @Autowired
    @Qualifier("Wordcount")
    Mapper wordcount;

    @Autowired
    Reducer reducer;

    @Autowired
    InverMapping inverMapping;

    @Autowired
    InverRedu inverRedu;

    @PostMapping("/process-task")
    HashMap<String, List<Integer>> checkmy(@RequestBody TaskRequest job){

        return wordcount.mapping(job.getLines());


    }


    @PostMapping("/reduce-task")
    HashMap<String,Integer> combining(@RequestBody map job){
        return reducer.reducing(job.getSection());

    }

    @PostMapping("/invertmap")
    HashMap<String,String> invermap(@RequestBody IndexReq req){
        return inverMapping.mapping(req.getFiles(),req.getFn());
    }

    @PostMapping("/invertredu")
    HashMap<String,String> inverred(@RequestBody inverjob job){
        return inverRedu.inv(job);
    }


}
