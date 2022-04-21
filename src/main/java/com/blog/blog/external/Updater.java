package com.blog.blog.external;

import java.io.File;
import java.util.Date;

public class Updater implements Runnable{
    static int lastId = 0;
    @Override
    public void run() {

        Database db = new Database();
        if(lastId==0) lastId = db.getLastItemId();
        System.out.println("Start id : " + lastId);
        db.disconnect();
        lastId = new DataFetcher().initializer(lastId);
//        new DataFetcher().sync("https://hacker-news.firebaseio.com/v0/updates.json?print=pretty");
        System.out.println("Updater Completed " + new Date().toString());
    }

    public static void main(String[] args){
        while (true){
            try {
                System.out.println("Updater On "+new Date().toString());
                new Thread(new Updater()).start();
                Thread.sleep(600000);
            }catch (Exception e){
                System.out.println("Fetch Interrupted "+e);
            }
        }
    }

}
