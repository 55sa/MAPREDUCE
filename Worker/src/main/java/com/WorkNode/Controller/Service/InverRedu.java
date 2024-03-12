package com.WorkNode.Controller.Service;

import Body.inverjob;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InverRedu {
    public HashMap<String,String> inv(inverjob job){
        HashMap<String,String> res=new HashMap<>();
        for (Map.Entry<String, List<String>> entry : job.getRes().entrySet()) {
            String word = entry.getKey();
            List<String> files = entry.getValue();

            // 将文件名列表转换为一个字符串，文件名之间用逗号分隔
            String filesStr = String.join(", ", files);

            // 将转换后的字符串放入结果HashMap中
            res.put(word, filesStr);
        }



        return res;
    }
}
