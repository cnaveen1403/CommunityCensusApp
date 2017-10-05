package com.zolipe.communitycensus.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.zolipe.communitycensus.model.Member;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHandler";
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "Census";

    // Member table name
    private static final String TABLE_MEMBER_INFO = "member_info";

    // Members Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_MEMBER_ID = "member_id";
    private static final String KEY_IS_HEAD = "isfamily_head";
    private static final String KEY_HEAD_ID = "family_head_id";
    private static final String KEY_RELATIONSHIP_ID = "relationship_id";
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_DOB = "dob";
    private static final String KEY_PH_NO = "phone_number";
    private static final String KEY_AADHAR_NO = "aadhar_number";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_CITY_ID = "city_id";
    private static final String KEY_STATE_ID = "state_id";
    private static final String KEY_COUNTRY = "country";
    private static final String KEY_ZIPCODE = "zipcode";
    private static final String KEY_USER_AVATAR = "user_avatar";
    private static final String KEY_IMAGE_TYPE = "image_type";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_ROLE_BASED_ID = "rolebased_user_id";
    private static final String KEY_CREATED_BY = "created_by";
    private static final String KEY_IS_SYNCED = "is_synced";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //Creating Tables
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        createMemberTable (sqLiteDatabase);
    }

    private void createMemberTable(SQLiteDatabase sqLiteDatabase) {
        String CREATE_MEMBER_TABLE = "CREATE TABLE " + TABLE_MEMBER_INFO + "("
                + KEY_ID + " INTEGER PRIMARY KEY, "
                + KEY_MEMBER_ID + " TEXT, "
                + KEY_IS_HEAD + " TEXT, "
                + KEY_HEAD_ID + " TEXT, "
                + KEY_RELATIONSHIP_ID + " TEXT, "
                + KEY_FIRST_NAME + " TEXT, "
                + KEY_LAST_NAME + " TEXT, "
                + KEY_GENDER + " TEXT, "
                + KEY_DOB + " TEXT, "
                + KEY_PH_NO + " TEXT, "
                + KEY_AADHAR_NO + " TEXT, "
                + KEY_EMAIL + " TEXT, "
                + KEY_ADDRESS + " TEXT, "
                + KEY_CITY_ID + " TEXT, "
                + KEY_STATE_ID + " TEXT, "
                + KEY_COUNTRY + " TEXT, "
                + KEY_ZIPCODE + " TEXT, "
                + KEY_USER_AVATAR + " TEXT, "
                + KEY_IMAGE_TYPE + " TEXT, "
                + KEY_USER_ROLE + " TEXT, "
                + KEY_ROLE_BASED_ID + " TEXT, "
                + KEY_IS_SYNCED + " TEXT, "
                + KEY_CREATED_BY + " TEXT)";
        sqLiteDatabase.execSQL(CREATE_MEMBER_TABLE);
    }

    //Upgrading Database
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //Drop older table if exists
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_MEMBER_INFO);

        //Create Tables Again
        onCreate(sqLiteDatabase);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new member
    public void addMember(Member member) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_MEMBER_ID, member.get_is_family_head());
        values.put(KEY_IS_HEAD, member.get_is_family_head());
        values.put(KEY_HEAD_ID, member.get_family_head_id());
        values.put(KEY_RELATIONSHIP_ID, member.get_relationship_id());
        values.put(KEY_FIRST_NAME, member.get_first_name());
        values.put(KEY_LAST_NAME, member.get_last_name());
        values.put(KEY_GENDER, member.get_gender());
        values.put(KEY_DOB, member.get_dob());
        values.put(KEY_PH_NO, member.get_phone_number());
        values.put(KEY_AADHAR_NO, member.get_aadhar_number());
        values.put(KEY_EMAIL, member.get_email());
        values.put(KEY_ADDRESS, member.get_address());
        values.put(KEY_CITY_ID, member.get_city_id());
        values.put(KEY_STATE_ID, member.get_state_id());
        values.put(KEY_COUNTRY, member.get_country());
        values.put(KEY_ZIPCODE, member.get_zipcode());
        values.put(KEY_USER_AVATAR, member.get_user_avatar());
//        values.put(KEY_IMAGE_TYPE, member.get_image_type());
        values.put(KEY_USER_ROLE, member.get_user_role());
        values.put(KEY_ROLE_BASED_ID, member.get_rolebased_user_id());
        values.put(KEY_CREATED_BY, member.get_created_by());

        // Inserting Row
        long id = db.insert(TABLE_MEMBER_INFO, null, values);
        Log.e(TAG, "addMember: After insert id >>> " + id);
        db.close(); // Closing database connection
    }

    // Getting single contact
    public Member getMember(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_MEMBER_INFO, new String[] { KEY_ID,
                        KEY_FIRST_NAME, KEY_PH_NO }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Member member = new Member(cursor.getString(cursor.getColumnIndex(KEY_IS_HEAD)),
                cursor.getString(cursor.getColumnIndex(KEY_HEAD_ID)),
                cursor.getString(cursor.getColumnIndex(KEY_RELATIONSHIP_ID)),
                cursor.getString(cursor.getColumnIndex(KEY_FIRST_NAME)),
                cursor.getString(cursor.getColumnIndex(KEY_LAST_NAME)),
                cursor.getString(cursor.getColumnIndex(KEY_GENDER)),
                cursor.getString(cursor.getColumnIndex(KEY_DOB)),
                cursor.getString(cursor.getColumnIndex(KEY_PH_NO)),
                cursor.getString(cursor.getColumnIndex(KEY_AADHAR_NO)),
                cursor.getString(cursor.getColumnIndex(KEY_EMAIL)),
                cursor.getString(cursor.getColumnIndex(KEY_ADDRESS)),
                cursor.getString(cursor.getColumnIndex(KEY_CITY_ID)),
                cursor.getString(cursor.getColumnIndex(KEY_STATE_ID)),
                cursor.getString(cursor.getColumnIndex(KEY_COUNTRY)),
                cursor.getString(cursor.getColumnIndex(KEY_ZIPCODE)),
                cursor.getString(cursor.getColumnIndex(KEY_USER_AVATAR)),
                cursor.getString(cursor.getColumnIndex(KEY_IMAGE_TYPE)),
                cursor.getString(cursor.getColumnIndex(KEY_USER_ROLE)),
                cursor.getString(cursor.getColumnIndex(KEY_ROLE_BASED_ID)),
                cursor.getString(cursor.getColumnIndex(KEY_CREATED_BY)));
        // return member
        return member;
    }

    // Getting All Contacts
    public List<Member> getAllMembers() {
        List<Member> memberList = new ArrayList<Member>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_MEMBER_INFO + " ORDER BY " + KEY_ID + " ASC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Member member = new Member();
                member.set_id(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
                member.set_is_family_head(cursor.getString(cursor.getColumnIndex(KEY_IS_HEAD)));
                member.set_family_head_id(cursor.getString(cursor.getColumnIndex(KEY_HEAD_ID)));
                member.set_relationship_id(cursor.getString(cursor.getColumnIndex(KEY_RELATIONSHIP_ID)));
                member.set_first_name(cursor.getString(cursor.getColumnIndex(KEY_FIRST_NAME)));
                member.set_last_name(cursor.getString(cursor.getColumnIndex(KEY_LAST_NAME)));
                member.set_gender(cursor.getString(cursor.getColumnIndex(KEY_GENDER)));
                member.set_dob(cursor.getString(cursor.getColumnIndex(KEY_DOB)));
                member.set_phone_number(cursor.getString(cursor.getColumnIndex(KEY_PH_NO)));
                member.set_aadhar_number(cursor.getString(cursor.getColumnIndex(KEY_AADHAR_NO)));
                member.set_email(cursor.getString(cursor.getColumnIndex(KEY_EMAIL)));
                member.set_address(cursor.getString(cursor.getColumnIndex(KEY_ADDRESS)));
                member.set_state_id(cursor.getString(cursor.getColumnIndex(KEY_STATE_ID)));
                member.set_city_id(cursor.getString(cursor.getColumnIndex(KEY_CITY_ID)));
                member.set_country(cursor.getString(cursor.getColumnIndex(KEY_COUNTRY)));
                member.set_zipcode(cursor.getString(cursor.getColumnIndex(KEY_ZIPCODE)));
                member.set_user_avatar(cursor.getString(cursor.getColumnIndex(KEY_USER_AVATAR)));
                member.set_image_type(cursor.getString(cursor.getColumnIndex(KEY_IMAGE_TYPE)));
                member.set_user_role(cursor.getString(cursor.getColumnIndex(KEY_USER_ROLE)));
                member.set_rolebased_user_id(cursor.getString(cursor.getColumnIndex(KEY_ROLE_BASED_ID)));
                member.set_created_by(cursor.getString(cursor.getColumnIndex(KEY_CREATED_BY)));
                // Adding member to list
                memberList.add(member);
            } while (cursor.moveToNext());
        }

        // return contact list
        return memberList;
    }

    // Updating single member
    public int updateMember(Member member) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_FIRST_NAME, member.get_first_name());
        values.put(KEY_PH_NO, member.get_phone_number());

        // updating row
        return db.update(TABLE_MEMBER_INFO, values, KEY_ID + " = ?",
                new String[] { String.valueOf(member.get_id()) });
    }

    // Deleting single member
    public void deleteMember(Member member) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MEMBER_INFO, KEY_ID + " = ?",
                new String[] { String.valueOf(member.get_id()) });
        db.close();
    }

    // Getting contacts Count
    public int getMembersCount() {
        String countQuery = "SELECT  * FROM " + TABLE_MEMBER_INFO;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }
}
