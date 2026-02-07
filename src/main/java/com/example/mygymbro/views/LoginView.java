package com.example.mygymbro.views;
import com.example.mygymbro.controller.LoginController;

public interface LoginView extends View {
    String getUsername();
    String getPassword();
    void setListener(LoginController controller);
}