package smarttrade.backend.TestDataUtil;

import smarttrade.backend.entities.userEntity;

public class userTestData {
    public static userEntity CreateUser(){
        return userEntity.builder().
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
}

