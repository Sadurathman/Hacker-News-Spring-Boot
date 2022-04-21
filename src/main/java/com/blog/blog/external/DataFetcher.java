package com.blog.blog.external;

import com.blog.blog.model.Item;
import com.blog.blog.model.User;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class DataFetcher {
    Database db;

    public String JSONString(URL url){
            try {
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.connect();

                int responseCode = con.getResponseCode();

                if (responseCode != 200) {
                    throw new RuntimeException("HttpResponseCode: " + responseCode);
                } else {
                    String inline = "";
                    Scanner sc = new Scanner(url.openStream());

                    while (sc.hasNext()) {
                        inline += sc.nextLine();
                    }

                    sc.close();

                    return inline;
                }
            } catch (Exception e) {
                System.out.println(e);
            }
            return null;
        }

    public JSONObject fetchData(URL url) {
        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.connect();

            int responseCode = con.getResponseCode();

            if (responseCode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responseCode);
            } else {
                String inline = "";
                Scanner sc = new Scanner(url.openStream());

                while (sc.hasNext()) {
                    inline += sc.nextLine();
                }

                sc.close();

                JSONParser parser = new JSONParser();
                return (JSONObject) parser.parse(inline);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    public int initializer(int lastItem){
        try {
            URL url = new URL("https://hacker-news.firebaseio.com/v0/maxitem.json?print=pretty");
            int maxId = Integer.parseInt(JSONString(url));

            System.out.println("Last id : "+ maxId);

            ArrayList<User> users = new ArrayList<>();
            HashMap<Integer, Item> items = new HashMap<>();

            if(lastItem==0) lastItem = maxId-20;

            db = new Database();

            HashSet<Integer> store = db.getAllItemId();

            db.disconnect();

            for(int i=lastItem;i<maxId;i++){
                Item item = itemData(i+"",users, store);
                if(item!=null){
                    if(item.getType().equals("comment")){
                        url = new URL("http://127.0.0.1:3001/api/item/"+item.getParent());

                        Item parent =  items.containsKey(item.getParent())? items.get(item.getParent()): getItemByURL(url);
                        if(parent == null) continue;
                        parent.getKids().add(item.getId());
                        parent.setDescendants(parent.getKids().size());

                        if(! items.containsKey(item.getParent()))
                            db.updateItem(parent);
                        else{
                            items.put(parent.getId(), parent);
                        }

                        item.setPoll(parent.getPoll());
                    }else{
                        item.setPoll(item.getId());
                    }
                    items.put(item.getId(),item);
                }
            }
            db = new Database();
            db.addUsers(users);
            db.addItems(new ArrayList<>(items.values()));
            return maxId;
        }catch (Exception e){
            System.out.println("Initializing failed : "+e);
        }finally {
            db.disconnect();
        }
        return 0;
    }

    public Item getItemByURL(URL url){
        try {
            JSONObject obj = fetchData(url);

            Item item = new Item();

            item.setId(Integer.parseInt(obj.get("id").toString()));
            item.setDeleted(obj.getOrDefault("deleted","false").toString().equals("true"));
            item.setType(obj.get("type").toString());
            item.setUser(obj.getOrDefault("by","").toString());
            item.setTime(Long.parseLong(obj.getOrDefault("time",0).toString()));
            item.setText(obj.getOrDefault("text","").toString());
            item.setDead(obj.getOrDefault("dead", "false").toString().equals("true"));
            item.setParent(Integer.parseInt(obj.getOrDefault("parent",0).toString()));
            item.setPoll(Integer.parseInt(obj.getOrDefault("poll",0).toString()));
            item.setKids(arrayToSet((JSONArray) obj.getOrDefault("kids", null)));
            item.setUrl(obj.getOrDefault("url","").toString());
            item.setScore(Integer.parseInt(obj.getOrDefault("score",0).toString()));
            item.setTitle(obj.getOrDefault("title","").toString());
            item.setParts(arrayToSet((JSONArray) obj.getOrDefault("parts",null)));
            item.setDescendants(Integer.parseInt(obj.getOrDefault("descendants",0).toString()));
        }catch (Exception e){
            System.out.println("Error Creating Item object From URL");
        }
        return null;
    }

    public Item itemData(String id, ArrayList<User> users, HashSet<Integer> store){
        try {
            URL url = new URL("https://hacker-news.firebaseio.com/v0/item/" + id + ".json?print=pretty");
            JSONObject obj = fetchData(url);

            Item item = new Item();

            item.setId(Integer.parseInt(obj.get("id").toString()));
            item.setDeleted(obj.getOrDefault("deleted","false").toString().equals("true"));
            item.setType(obj.get("type").toString());
            item.setUser(obj.getOrDefault("by","").toString());
            item.setTime(Long.parseLong(obj.getOrDefault("time",0).toString()));
            item.setText(obj.getOrDefault("text","").toString());
            item.setDead(obj.getOrDefault("dead", "false").toString().equals("true"));
            item.setParent(Integer.parseInt(obj.getOrDefault("parent",0).toString()));
            item.setPoll(Integer.parseInt(obj.getOrDefault("poll",0).toString()));
            item.setKids(arrayToSet((JSONArray) obj.getOrDefault("kids", null)));
            item.setUrl(obj.getOrDefault("url","").toString());
            item.setScore(Integer.parseInt(obj.getOrDefault("score",0).toString()));
            item.setTitle(obj.getOrDefault("title","").toString());
            item.setParts(arrayToSet((JSONArray) obj.getOrDefault("parts",null)));
            item.setDescendants(Integer.parseInt(obj.getOrDefault("descendants",0).toString()));

            if((item.getParent()==0 && !store.contains(item.getId()))|| store.contains(item.getParent())) {
                users.add(userData(item.getUser()));
                store.add(item.getId());
                return item;
            }
        }catch (Exception e){
            System.out.println("Error Creating Item object "+id);
        }
        return null;
    }

    public HashSet<Integer> arrayToSet(JSONArray ar){
        HashSet<Integer> set = new HashSet<>();
        if(ar == null) return set;
        for (Object o : ar) set.add(Integer.parseInt(o.toString()));
        return set;
    }

    public User userData(String user) {
        try {
            URL url = new URL("https://hacker-news.firebaseio.com/v0/user/" + user + ".json?print=pretty");

            JSONObject obj = fetchData(url);

            User data = new User();

            data.setId(obj.get("id").toString());
            data.setAbout(obj.getOrDefault("about","").toString());
            data.setCreated(Long.parseLong(obj.get("created").toString()));
            data.setKarma(Integer.parseInt(obj.get("karma").toString()));
            data.setSubmitted(arrayToSet((JSONArray)obj.get("submitted")));

            return data;

        }catch (Exception e){
            System.out.println("Error creating user "+user);
        }
        return null;
    }

    public void sync(String base){
        try {
            URL url = new URL(base);

            JSONObject obj = null;
            JSONArray items = null;
            JSONArray profiles = null;
//            if (type.equals("updates")){
                obj = fetchData(url);
                profiles = (JSONArray) obj.get("profiles");
                items = (JSONArray) obj.get("items");
//            }
//            else
////            if(type.equals("newstories"))
//            {
//                items = (JSONArray) JSONValue.parse(JSONString(url));
//            }
            db = new Database();
            ArrayList<User> users = new ArrayList<>();
            ArrayList<Item> item = new ArrayList<>();
            HashSet<Integer> store = db.getAllItemId();

            if(profiles!=null) {
                for (Object profile : profiles) users.add(userData(profile.toString()));
            }

            if(items != null) {
                for (Object o : items) {
                    Item curItem = itemData(o.toString(), users, store);
                    if (curItem != null) {
                        item.add(curItem);
                        store.add(curItem.getId());
                        if (store.contains(curItem.getId())) {
                            HashSet<Integer> kids = db.getKidsById(curItem.getId());
                            curItem.getKids().addAll(kids);
                            curItem.setDescendants(curItem.getKids().size());
                            db.updateItem(curItem);
                        }
                    }
                }
            }

            System.out.println("Updated Items : "+item.size());
            System.out.println("Updated Users : "+users.size());
//            db.addItems(item);
//            db.addUsers(users);


        }catch (Exception e){
            System.out.println("Error in Sync : \n"+e);
        }finally {
            db.disconnect();
        }
    }

    protected void finalize(){
        System.out.println("The Update has been Made !!!");
    }

    public static void main(String[] args){
        new DataFetcher().initializer(31093449);
    }
}
