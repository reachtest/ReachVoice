package za.co.inventit.reachvoice;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by pieter on 2017/07/20.
 */
public class RealmUser extends RealmObject {

    @PrimaryKey
    String key;
    String name;

    public RealmUser() {
    }

    public RealmUser(String key, String name) {
        this.key = key;
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
