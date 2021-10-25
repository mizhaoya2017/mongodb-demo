package com.example.mongodbdemo.service.impl;

import com.example.mongodbdemo.entity.ResourceHostReplaceEntity;
import com.example.mongodbdemo.entity.ResourceHostReplaceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @program: optrace
 * @description:
 * @author: Logan
 * @create: 2020-09-22 21:42
 **/
@Service
@Slf4j
public class ResourceHostReplaceServiceImpl implements ResourceHostReplaceService {
//    @Autowired
//    private ResourceHostReplaceMapper resourceHostReplaceMapper;
    @Override
    public List<ResourceHostReplaceEntity> getResourceHostReplaceList() {
//        return resourceHostReplaceMapper.selectResourceHostReplace();
        return null;
    }
}
