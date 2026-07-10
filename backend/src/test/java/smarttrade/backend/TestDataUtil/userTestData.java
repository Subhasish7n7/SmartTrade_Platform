package smarttrade.backend.TestDataUtil;

import smarttrade.backend.entities.UserEntity;

public class userTestData {
    public static UserEntity CreateUserA(){
        return UserEntity.builder().
                email("hari@gmail.com").
                name("hari mishra").
                phone_no("+91 9999636321").
                trustScore(8.28).
                totalListings(1).
                successfulTrades(0).
                latitude(35.6895). // Tokyo, Japan
                longitude(139.6917).
                build();
    }
    public static UserEntity CreateUserB(){
        return UserEntity.builder().
                email("rama@gmail.com").
                name("rama baidya").
                phone_no("+91 9999639872").
                trustScore(9.56).
                totalListings(3).
                successfulTrades(2).
                latitude(35.6895). // Tokyo, Japan
                        longitude(139.6917).
                build();
    }
    public static UserEntity CreateUserC(){
        return UserEntity.builder().
                email("shyam@gmail.com").
                name("shaym gantayata").
                phone_no("+91 7788866655").
                trustScore(6.25).
                totalListings(7).
                successfulTrades(5).
                latitude(35.6895). // Tokyo, Japan
                        longitude(139.6917).
                build();
    }
}

