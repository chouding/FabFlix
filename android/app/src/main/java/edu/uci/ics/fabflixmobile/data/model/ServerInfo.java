package edu.uci.ics.fabflixmobile.data.model;

public class ServerInfo {
    public static String getServerUrl(){
//        String host = "10.0.2.2";
        String host = "54.183.200.3";
//        String port = "8080";
        String port = "8443";
//        String domain = "cs122b_spring23_project_war_exploded";
        String domain = "cs122b-spring23-project";
        return "https://" + host + ":" + port + "/" + domain;
    }
}
