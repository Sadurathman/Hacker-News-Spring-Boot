package com.blog.blog.repo;

import com.blog.blog.model.User;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface UserRepo extends CassandraRepository<User, String> {
    public User getUserByIdEquals(String id);
}
