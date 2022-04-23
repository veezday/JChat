package ru.veezeday.dev.dao;

import java.io.FileReader;
import java.io.IOException;

import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public abstract class DataAccessObject {
    public static String URL;
    public static String USERNAME;
    public static String PASSWORD;
    private static final Properties properties = new Properties();

    static {
        try {
            properties.load(new FileReader("C:\\Users\\MLEGION.RU\\IdeaProjects\\JChat\\src\\main\\resources\\server.properties"));
            URL = properties.getProperty("URL");
            USERNAME = properties.getProperty("serverUserName");
            PASSWORD = properties.getProperty("serverUserPassword");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
