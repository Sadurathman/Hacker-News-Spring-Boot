package com.blog.blog.model;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.Set;

@Table("user")
public class User {

    @PrimaryKey
    private String id;

    private long created;
    private int karma;
    private String about;
    private Set<Integer> submitted;
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public String getAbout() {
        return about;
    }

    public long getCreated() {
        return created;
    }

    public int getKarma() {
        return karma;
    }

    public Set<Integer> getSubmitted() {
        return submitted;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public void setKarma(int karma) {
        this.karma = karma;
    }

    public void setSubmitted(Set<Integer> submitted) {
        this.submitted = submitted;
    }

    public String toString(){
        return getId();
    }
}
