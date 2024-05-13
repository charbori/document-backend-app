package main.blog.domain.service;

import main.blog.domain.dto.VideoCategoryDTO;
import main.blog.domain.entity.VideoCategoryEntity;
import main.blog.domain.repository.UserRepository;
import main.blog.domain.repository.VideoCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VideoCategoryService {
    @Autowired
    private VideoCategoryRepository videoCategoryRepository;

    public List<VideoCategoryDTO> getVideoCategoryList() {
        List<VideoCategoryEntity> videoCategoryEntityList = videoCategoryRepository.findAll();
        List<VideoCategoryDTO> videoCategoryDTOList = videoCategoryEntityList.stream().map(m -> (
                new VideoCategoryDTO(m.getId(), m.getName(), m.getRole())
        ))
        .collect(Collectors.toList());
        return videoCategoryDTOList;
    }
}
