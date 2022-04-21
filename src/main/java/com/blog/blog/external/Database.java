package com.blog.blog.external;

import com.blog.blog.model.Item;
import com.blog.blog.model.User;
import com.datastax.driver.core.*;
import org.omg.DynamicAny.DynArray;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Database {
    private Cluster cluster;
    private Session session;

    public Database(){
        String IP = "127.0.0.1";
        int PORT = 9042;

        try {
            Cluster.Builder b = Cluster.builder().addContactPoint(IP);
            b.withPort(PORT);

            cluster = b.build();

            session = cluster.connect();
            System.out.println("connected to Database");
        }catch(Exception e){
            System.out.println("Unable to connect to Database");
        }
    }

    public void disconnect(){
        cluster.close();
        session.close();
    }

    public void addUsers(ArrayList<User> users){
        try {
            String query = "insert into db.user(id, about, created, karma, submitted) values (:id, :about, :created, :karma, :submitted);";
            PreparedStatement ps = session.prepare(query);
            for(User user: users){
                BoundStatement bs = ps.bind(user.getId(), user.getAbout(), new BigInteger(user.getCreated() + ""), user.getKarma(), user.getSubmitted());
                session.execute(bs);
            }
            System.out.println("Batch of Users Added");
        }catch (Exception e){
            System.out.println("Unable to add users "+ e);
        }
    }

    public void addItems(ArrayList<Item> items){
        try {
            String query = "insert into db.item(id, dead, deleted, descendants, kids, parent, parts, poll, score, text, time, title, type, url, user) values (:id, :dead, :deleted, :descendants, :kids, :parent, :parts, :poll, :score, :text, :time, :title, :type, :url, :user);";
            PreparedStatement ps = session.prepare(query);
            for(Item item: items){
                BoundStatement bs = (ps.bind(item.getId(), item.isDead(), item.isDeleted(), item.getDescendants(), item.getKids(), item.getParent(), item.getParts(), item.getPoll(), item.getScore(), item.getText(), new BigInteger(item.getTime()+""), item.getTitle(), item.getType(), item.getUrl(), item.getUser()));
                session.execute(bs);
            }
            System.out.println("Batch of Items Added");
        }catch (Exception e){
            System.out.println("Unable to add Items ");
        }
    }

    public Item updateItem(Item item){
        try {
            String query = "update db.item set kids = :kids, descendants = :descendants where id = :id";
            PreparedStatement ps = session.prepare(query);
            session.execute(ps.bind(item.getKids(), item.getDescendants()+1, item.getId()));
            System.out.println("Item Updated");
            return item;
        }catch (Exception e){
            System.out.println("Unable to update Item "+e);
        }
        return null;
    }

    public HashSet<Integer> getAllItemId(){
        HashSet<Integer> ids = new HashSet<Integer>();
        try{
            String query = "select id from db.item;";
            ResultSet rs = session.execute(query);
            rs.all().forEach(r-> ids.add(r.getInt(0)));
            System.out.println("Initial Store : "+ids);
        }catch (Exception e){
            System.out.println("Unable to get All Item Ids");
        }
        return ids;
    }

    public int getLastItemId(){
        try{
            String query = "select id from db.item;";
            ResultSet rs = session.execute(query);
            return rs.one().getInt(0);
        }catch (Exception e){
            System.out.println("Unable to get Last Id");
        }
        return 0;
    }

    public HashSet<Integer> getKidsById(int id){
        try{
            HashSet<Integer> kids;
            String query = "select kids from db.item where id=:id";
            PreparedStatement ps = session.prepare(query);
            ResultSet rs = session.execute(ps.bind(id));
            kids = rs.one().get(0, HashSet.class);
            return kids;
        }catch (Exception e){
            System.out.println("Unable to get All Item Ids");
        }
        return null;
    }

    public User updateUser(User user){
        try {
            String query = "update db.user set submitted = :submitted where id = :id";
            PreparedStatement ps = session.prepare(query);
            session.execute(ps.bind(user.getSubmitted(), user.getId()));
            System.out.println("User Updated");
            return user;
        }catch (Exception e){
            System.out.println("Unable to update User "+e);
        }
        return null;
    }
}
