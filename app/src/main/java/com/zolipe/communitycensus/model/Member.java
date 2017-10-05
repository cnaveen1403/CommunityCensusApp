package com.zolipe.communitycensus.model;

public class Member {
    //private variables
    int _id;
    String _is_family_head;
    String _family_head_id;
    String _relationship_id;
    String _first_name;
    String _last_name;
    String _gender;
    String _dob;
    String _phone_number;
    String _aadhar_number;
    String _email;
    String _address;
    String _city_id;
    String _state_id;
    String _country;
    String _zipcode;
    String _user_avatar;
    String _image_type;
    String _user_role;
    String _rolebased_user_id;
    String _created_by;

    // Empty constructor
    public Member(){
    }

    // constructor
    public Member(int _id, String _is_family_head, String _family_head_id,
                  String _relationship_id, String _first_name,
                  String _last_name, String _gender, String _dob,
                  String _phone_number, String _aadhar_number, String _email,
                  String _address, String _city_id, String _state_id,
                  String _country, String _zipcode, String _user_avatar,
                  String _image_type, String _user_role, String _rolebased_user_id,
                  String _created_by) {
        this._id = _id;
        this._is_family_head = _is_family_head;
        this._family_head_id = _family_head_id;
        this._relationship_id = _relationship_id;
        this._first_name = _first_name;
        this._last_name = _last_name;
        this._gender = _gender;
        this._dob = _dob;
        this._phone_number = _phone_number;
        this._aadhar_number = _aadhar_number;
        this._email = _email;
        this._address = _address;
        this._city_id = _city_id;
        this._state_id = _state_id;
        this._country = _country;
        this._zipcode = _zipcode;
        this._user_avatar = _user_avatar;
        this._image_type = _image_type;
        this._user_role = _user_role;
        this._rolebased_user_id = _rolebased_user_id;
        this._created_by = _created_by;
    }

    // constructor
    public Member(String _is_family_head, String _family_head_id, String _relationship_id,
                  String _first_name, String _last_name, String _gender,
                  String _dob, String _phone_number, String _aadhar_number,
                  String _email, String _address, String _city_id,
                  String _state_id, String _country, String _zipcode,
                  String _user_avatar, String _image_type, String _user_role,
                  String _rolebased_user_id, String _created_by) {
        this._is_family_head = _is_family_head;
        this._family_head_id = _family_head_id;
        this._relationship_id = _relationship_id;
        this._first_name = _first_name;
        this._last_name = _last_name;
        this._gender = _gender;
        this._dob = _dob;
        this._phone_number = _phone_number;
        this._aadhar_number = _aadhar_number;
        this._email = _email;
        this._address = _address;
        this._city_id = _city_id;
        this._state_id = _state_id;
        this._country = _country;
        this._zipcode = _zipcode;
        this._user_avatar = _user_avatar;
        this._image_type = _image_type;
        this._user_role = _user_role;
        this._rolebased_user_id = _rolebased_user_id;
        this._created_by = _created_by;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String get_is_family_head() {
        return _is_family_head;
    }

    public void set_is_family_head(String _is_family_head) {
        this._is_family_head = _is_family_head;
    }

    public String get_family_head_id() {
        return _family_head_id;
    }

    public void set_family_head_id(String _family_head_id) {
        this._family_head_id = _family_head_id;
    }

    public String get_relationship_id() {
        return _relationship_id;
    }

    public void set_relationship_id(String _relationship_id) {
        this._relationship_id = _relationship_id;
    }

    public String get_first_name() {
        return _first_name;
    }

    public void set_first_name(String _first_name) {
        this._first_name = _first_name;
    }

    public String get_last_name() {
        return _last_name;
    }

    public void set_last_name(String _last_name) {
        this._last_name = _last_name;
    }

    public String get_gender() {
        return _gender;
    }

    public void set_gender(String _gender) {
        this._gender = _gender;
    }

    public String get_dob() {
        return _dob;
    }

    public void set_dob(String _dob) {
        this._dob = _dob;
    }

    public String get_phone_number() {
        return _phone_number;
    }

    public void set_phone_number(String _phone_number) {
        this._phone_number = _phone_number;
    }

    public String get_aadhar_number() {
        return _aadhar_number;
    }

    public void set_aadhar_number(String _aadhar_number) {
        this._aadhar_number = _aadhar_number;
    }

    public String get_email() {
        return _email;
    }

    public void set_email(String _email) {
        this._email = _email;
    }

    public String get_address() {
        return _address;
    }

    public void set_address(String _address) {
        this._address = _address;
    }

    public String get_city_id() {
        return _city_id;
    }

    public void set_city_id(String _city_id) {
        this._city_id = _city_id;
    }

    public String get_state_id() {
        return _state_id;
    }

    public void set_state_id(String _state_id) {
        this._state_id = _state_id;
    }

    public String get_country() {
        return _country;
    }

    public void set_country(String _country) {
        this._country = _country;
    }

    public String get_zipcode() {
        return _zipcode;
    }

    public void set_zipcode(String _zipcode) {
        this._zipcode = _zipcode;
    }

    public String get_user_avatar() {
        return _user_avatar;
    }

    public void set_user_avatar(String _user_avatar) {
        this._user_avatar = _user_avatar;
    }

    public String get_image_type() {
        return _image_type;
    }

    public void set_image_type(String _image_type) {
        this._image_type = _image_type;
    }

    public String get_user_role() {
        return _user_role;
    }

    public void set_user_role(String _user_role) {
        this._user_role = _user_role;
    }

    public String get_rolebased_user_id() {
        return _rolebased_user_id;
    }

    public void set_rolebased_user_id(String _rolebased_user_id) {
        this._rolebased_user_id = _rolebased_user_id;
    }

    public String get_created_by() {
        return _created_by;
    }

    public void set_created_by(String _created_by) {
        this._created_by = _created_by;
    }
}
