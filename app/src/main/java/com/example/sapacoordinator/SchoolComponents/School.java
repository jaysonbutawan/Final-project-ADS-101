package com.example.sapacoordinator.SchoolComponents;

import com.google.gson.annotations.SerializedName;

public class School {
    @SerializedName("school_id")
    private int id;
        @SerializedName("school_name")
        private String name;

        @SerializedName("status")
        private String status;

        @SerializedName("school_address")
        private String address;

        @SerializedName("contact_info")
        private String contact;

        @SerializedName("created_at")
        private String addedDate;

        public School(int id,String name, String status, String address, String contact, String addedDate) {
            this.id = id;
            this.name = name;
            this.status = status;
            this.address = address;
            this.contact = contact;
            this.addedDate = addedDate;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getStatus() { return status; }
        public String getAddress() { return address; }
        public String getContact() { return contact; }
        public String getAddedDate() { return addedDate; }

    }


