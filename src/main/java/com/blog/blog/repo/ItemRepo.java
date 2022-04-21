package com.blog.blog.repo;

import com.blog.blog.model.Item;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import java.util.*;

public interface ItemRepo extends CassandraRepository<Item, Integer>{
    List<Item> findItemByType(String type);
    List<Item> findItemsByParentEquals(int id);
    List<Item> findItemsByPollEquals(int id);
    Item getItemByIdEquals(int id);
    @Query("update items set kids = :kids where id = :id")
    Item updateItemByIdEquals(int id, Set<Integer> kids);
    List<Item> findItemsByIdIn(Set<Integer> ids);
}
