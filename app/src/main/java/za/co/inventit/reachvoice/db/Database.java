package za.co.inventit.reachvoice.db;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.exceptions.RealmMigrationNeededException;

/**
 * Created by pieter on 2017/07/20.
 */
public class Database {
    private static final String TAG = Database.class.getSimpleName();

    public static void init(Context context) {
        Realm.init(context);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .schemaVersion(0) // Must be bumped when the schema changes
                .deleteRealmIfMigrationNeeded()
                .build();

        Realm.setDefaultConfiguration(config);

        Realm realm = Realm.getInstance(config);
        realm.close();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // USER
    ///////////////////////////////////////////////////////////////////////////////////////////////
    public static class User {

        public static boolean save(final RealmUser user) {
            Realm realm = null;
            try {
                realm = Realm.getDefaultInstance();

                realm.beginTransaction();
                realm.copyToRealmOrUpdate(user);
                realm.commitTransaction();
                return true;
            } catch (RealmMigrationNeededException rmne) {
                Log.e(TAG, "Realm migration exception");
                return false;
            } catch (Exception e) {
                if (realm != null) {
                    realm.cancelTransaction();
                }
                Log.e(TAG, "DB Error: User.save", e);
                return false;
            } finally {
                if (realm != null) {
                    realm.close();
                }
            }
        }

        public static RealmUser get(String key) {
            Realm realm = null;
            try {
                realm = Realm.getDefaultInstance();
                RealmResults<RealmUser> r = realm.where(RealmUser.class).equalTo(key, "key").findAll();
                if (r.size() > 0) {
                    return r.first();
                }
                else {
                    return null;
                }
            } catch (RealmMigrationNeededException rmne) {
                Log.e(TAG, "Realm migration exception");
                return null;
            } catch (Exception e) {
                if (realm != null) {
                    realm.cancelTransaction();
                }
                Log.e(TAG, "DB Error: UserProfile.get", e);
                return null;
            } finally {
                if (realm != null) {
                    realm.close();
                }
            }
        }

         public static List<RealmUser> getAll() {
            ArrayList<RealmUser> users = null;
            Realm realm = null;
            try {
                realm = Realm.getDefaultInstance();
                RealmResults<RealmUser> results = realm.where(RealmUser.class).findAllSorted("name");

                users = new ArrayList<>();

                for (int i = 0; i < results.size(); i++) {
                    users.add(results.get(i));
                }
            } catch (RealmMigrationNeededException rmne) {
                Log.e(TAG, "Realm migration exception");
            } catch (Exception e) {
                if (realm != null) {
                    realm.cancelTransaction();
                }
                Log.e(TAG, "DB Error: User.getAll", e);
            } finally {
                if (realm != null) {
                    realm.close();
                }
            }

            return users;
        }
    }
}
