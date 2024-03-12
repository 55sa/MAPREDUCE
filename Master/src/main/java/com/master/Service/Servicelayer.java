package com.master.Service;

import Body.JobRequest;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public interface Servicelayer {
    public ResponseEntity<String> dispatchTasks(JobRequest jobRequest) throws IOException;
}
