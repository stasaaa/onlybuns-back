package com.isa.onlybuns_back.iservice;

import com.isa.onlybuns_back.model.Post;

import java.util.List;
import java.util.Optional;

public interface IPostService {

    Optional<Post> getById(Long id);

    List<Post> getAll();

    Post create(Post post);

    void delete(Long id);

    Optional<Post> update(Long id, Post postDetails);

    List<Post> getByUserId(Long userId);
}
