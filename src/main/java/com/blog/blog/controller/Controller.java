package com.blog.blog.controller;

import com.blog.blog.external.Database;
import com.blog.blog.model.Item;
import com.blog.blog.model.User;
import com.blog.blog.repo.ItemRepo;
import com.blog.blog.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api")
public class Controller {

    @Autowired
    ItemRepo itemRepo;
    @Autowired
    UserRepo userRepo;

    @GetMapping(path = "")
    public List<Item> getHomeStories(){
        System.out.println("Get all Stories");

        List<Item> items = itemRepo.findAll();
        Set<Item> active = new LinkedHashSet<>();

        HashMap<Integer, Item> itemFetcher = new HashMap<>();
        for(Item item: items){
            itemFetcher.put(item.getId(), item);
        }
        Collections.sort(items, Collections.reverseOrder());

            for (Item item : items) {
                Item temp = item;
                while (temp != null && temp.getParent()!=0)
                    temp = itemFetcher.get(temp.getParent());
                if(temp!=null) active.add(temp);
            }

        return active.stream().collect(Collectors.toList());
    }

    @GetMapping("/users")
    public List<User> getAllUsers(){
        System.out.println("Get All Users");

        return userRepo.findAll();
    }

    @PostMapping("/user/register")
    public ResponseEntity<User> createUser(@RequestBody User user){
        user.setCreated(new Date().getTime()/1000);
        User _user = userRepo.save(user);
        System.out.println("Created User : "+user.getId());
        return new ResponseEntity<>(_user, HttpStatus.OK);
    }

    @PostMapping("/user/login")
    public ResponseEntity<User> loginUser(@RequestBody User user){

        User _user = userRepo.getUserByIdEquals(user.getId());
        System.out.println("User Found : "+_user.getId());
        if(_user.getPassword().equals(user.getPassword()))
            return new ResponseEntity<>(_user, HttpStatus.OK);
        else return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<User> getUser(@PathVariable String id){
        System.out.println("Find User with id : "+id);
        User user =  userRepo.getUserByIdEquals(id);
        if(user == null) return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("/item/{id}")
    public Item getItem(@PathVariable String id){
        System.out.println("Find Item id from getItem() : "+id);
        return itemRepo.getItemByIdEquals(Integer.parseInt(id));
    }

    public void fetchNestedComments(int parentId, HashMap<Integer, Item> comments){
        if(parentId == 0) return ;
        List<Item> items = itemRepo.findItemsByParentEquals(parentId);
        for(Item item: items){
            if(item==null) continue;
            comments.put(item.getId(), item);
            fetchNestedComments(item.getId(), comments);
        }
    }


    @GetMapping("/item/comments/{id}")
    public HashMap<Integer, Item> getComments(@PathVariable int id){
        System.out.println("Find Comments for Item id : "+id);
        HashMap<Integer, Item> comment = new HashMap<>();
        List<Item> items = itemRepo.findItemsByPollEquals(id);
        if(items == null) return null;
        for(Item item: items ) comment.put(item.getId(), item);
//        fetchNestedComments(item.getId(), comment);
        return comment;
    }

    @GetMapping("/items")
    public List<Item> getAllItems(){
        System.out.println("Get All Items");

        return itemRepo.findAll();
    }

    @GetMapping("/new")
    public List<Item> getAllNewItems(){
        System.out.println("Get All Recent Stories");
        List<Item> item = itemRepo.findItemByType("story");
        Collections.sort(item, Collections.reverseOrder());
        return item;
    }

    @GetMapping("/past")
    public List<Item> getAllPastItems(){
        System.out.println("Get All Past Stories");
        List<Item> item = itemRepo.findItemByType("story");
        Collections.sort(item);
        return item;
    }

    @GetMapping("/comment")
    public List<Item> getAllComment(){
        System.out.println("Get All Recent Comment");
        List<Item> item = itemRepo.findItemByType("comment");
        Collections.sort(item, Collections.reverseOrder());
        return item;
    }

    @PostMapping("/item/new-comment/{id}")
    public ResponseEntity<Item> makeComment(@RequestBody Item comment, @PathVariable int id){
        try {
            System.out.println("User : " + comment.getUser() + "\nComment : " + comment.getText());
            Item item = itemRepo.getItemByIdEquals(id);
            comment.setPoll(item.getPoll());
            ResponseEntity<Item> createComment = createItem(comment);
            System.out.println("Updating item...");
            if(item.getKids()==null) item.setKids(new HashSet<>());
            item.getKids().add(createComment.getBody().getId());
            System.out.println("Updating in database...");
            Database db = new Database();
            db.updateItem(item);
            db.disconnect();
            System.out.println("Comment updated : " + comment.getText() + " id : " + item.getId());
            return new ResponseEntity<>(item, HttpStatus.OK);
        }catch (Exception e){
            System.out.println(e);
        }
        return null;
    }

    @PostMapping("/item/create")
    public ResponseEntity<Item> createItem(@RequestBody Item item){
        if(item.getId() == 0) item.setId( (int) (System.currentTimeMillis() & 0xfffffff));
        System.out.println("Created Item : "+item.getId()+" of type "+item.getType());
        if(item.getType().equals("story")) item.setPoll(item.getId());
        User user = userRepo.getUserByIdEquals(item.getUser());
        if(user.getSubmitted()==null) user.setSubmitted(new HashSet<>());
        user.getSubmitted().add(item.getId());
        item.setTime(new Date().getTime()/1000);
        Database db = new Database();
        db.updateUser(user);
        db.disconnect();
        Item _item = itemRepo.save(item);
        return new ResponseEntity<>(_item, HttpStatus.OK);
    }
}
