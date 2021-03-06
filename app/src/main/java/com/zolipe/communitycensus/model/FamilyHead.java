package com.zolipe.communitycensus.model;

import android.os.Parcel;
import android.os.Parcelable;

public class FamilyHead implements Parcelable {
    private String first_name;
    private String last_name;
    private String phone_number;
    private String aadhaar;
    private String email;
    private String address;
    private String gender;
    private String age;
    private String image_url;
    private String relationship;
    private String family_size;
    private String zipcode;
    private String dob;
    private String familyHeadId;
    private String isFamilyHead;
    private String isSynced;
    private String state_id;
    private String city_id;

    public FamilyHead(String first_name, String last_name,
                      String phone, String aadhaar, String email,
                      String address, String gender, String url,
                      String age, String relationship, String family_size,
                      String zipcode, String dob, String familyHeadId, String isFamilyHead,
                      String isSynced, String state_id, String city_id) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.phone_number = phone;
        this.aadhaar = aadhaar;
        this.email = email;
        this.address = address;
        this.gender = gender;
        this.age = age;
        this.image_url = url;
        this.relationship = relationship;
        this.family_size = family_size;
        this.zipcode = zipcode;
        this.dob = dob;
        this.familyHeadId = familyHeadId;
        this.isFamilyHead = isFamilyHead;
        this.isSynced = isSynced;
        this.state_id = state_id;
        this.city_id = city_id;
    }

    protected FamilyHead(Parcel in) {
        first_name = in.readString();
        last_name = in.readString();
        phone_number = in.readString();
        aadhaar = in.readString();
        email = in.readString();
        address = in.readString();
        gender = in.readString();
        age = in.readString();
        image_url = in.readString();
        relationship = in.readString();
        family_size = in.readString();
        zipcode = in.readString();
        dob = in.readString();
        familyHeadId = in.readString();
        isFamilyHead = in.readString();
        isSynced = in.readString();
        state_id = in.readString();
        city_id = in.readString();
    }

    public static final Creator<FamilyHead> CREATOR = new Creator<FamilyHead>() {
        @Override
        public FamilyHead createFromParcel(Parcel in) {
            return new FamilyHead(in);
        }

        @Override
        public FamilyHead[] newArray(int size) {
            return new FamilyHead[size];
        }
    };

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getAadhaar() {
        return aadhaar;
    }

    public void setAadhaar(String aadhaar) {
        this.aadhaar = aadhaar;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getFamily_size() {
        return family_size;
    }

    public void setFamily_size(String family_size) {
        this.family_size = family_size;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getFamilyHeadId() {
        return familyHeadId;
    }

    public void setFamilyHeadId(String familyHeadId) {
        this.familyHeadId = familyHeadId;
    }

    public String getIsFamilyHead() {
        return isFamilyHead;
    }

    public void setIsFamilyHead(String isFamilyHead) {
        this.isFamilyHead = isFamilyHead;
    }

    public String getIsSynced() {
        return isSynced;
    }

    public void setIsSynced(String isSynced) {
        this.isSynced = isSynced;
    }

    public String getState_id() {
        return state_id;
    }

    public void setState_id(String state_id) {
        this.state_id = state_id;
    }

    public String getCity_id() {
        return city_id;
    }

    public void setCity_id(String city_id) {
        this.city_id = city_id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(first_name);
        dest.writeString(last_name);
        dest.writeString(phone_number);
        dest.writeString(aadhaar);
        dest.writeString(email);
        dest.writeString(address);
        dest.writeString(gender);
        dest.writeString(age);
        dest.writeString(image_url);
        dest.writeString(relationship);
        dest.writeString(family_size);
        dest.writeString(zipcode);
        dest.writeString(dob);
        dest.writeString(familyHeadId);
        dest.writeString(isFamilyHead);
        dest.writeString(isSynced);
        dest.writeString(state_id);
        dest.writeString(city_id);
    }
}
