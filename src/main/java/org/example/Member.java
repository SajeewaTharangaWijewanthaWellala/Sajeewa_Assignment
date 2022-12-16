package org.example;

    public class Member {
        int id;
        public String Name;
        public String Age;
        public String Address;

        public static Member of(String name, String Age, String Address){
            Member member = new Member();
            member.Name = name;
            member.Age = Age;
            member.Address = Address;
            return member;
        }
    }
