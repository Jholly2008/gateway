package com.example.demo.gateway;

import com.example.demo.gateway.utils.JwtUtils;

public class Test {
    public static void main(String[] args) {

        String token = JwtUtils.generateToken("ggg");

        System.out.println(token);

        System.out.println(JwtUtils.getTenantFromToken(token));
    }
}
