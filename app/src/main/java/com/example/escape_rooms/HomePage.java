package com.example.escape_rooms;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomePage extends Fragment {


    public HomePage() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.homepage, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        setUpRecyclerView(view);
        SetUpLER(view);
        SetUpCR(view);
    }
    private Button MyER = null;
    private Button CER = null;
    private Button CR = null;
    private Button SER = null;
    private Button LER = null;
    private ArrayList<User> List = null;
    UserRepository userRepository = new UserRepository();
    User user = null;
    private Room[] rooms = {
            new Room("Easy", 1, 5),
            new Room("Medium", 2, 7),
            new Room("Hard", 3, 9)
    };
    private void setUpRecyclerView(View view){
        MyER = getView().findViewById(R.id.MyER);
        CER = getView().findViewById(R.id.CER);
        CR = getView().findViewById(R.id.CR);
        SER = getView().findViewById(R.id.SER);
        LER = getView().findViewById(R.id.LER);
    }
    public void SetUpMyER(View view) {//my levels
        MyER.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            }
        });
    }

    public void SetUpCER(View view) {//community rooms*
        CER.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    public void SetUpCR(View view) {//create room
        CR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(Room room :rooms) {
                    createRoomXML(room);
                    System.out.println("XML files created successfully!");
                     //View name= getLayoutInflater().inflate(R.layout."room_" + room.getId() + ".xml", null);
                }
            }
        });
    }

    public void SetUpSER(View view) {//sign up, see if the user is in the list and move him to the next page
        SER.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //debug
                System.out.println("point");
                //                View dialogView = getLayoutInflater().inflate(R.layout.sign_in, null);
//                userRepository.getList(new UserRepository.UsersCallback<List<User>>() {
//                    @Override
//                    public void onSuccess(List<User> result) {
//                        EditText usernameET = dialogView.findViewById(R.id.ETusername);
//                        EditText passwordET = dialogView.findViewById(R.id.ETpassword);
//                        String newusername = usernameET.toString();
//                        String newpassword = passwordET.toString();
//                        for (User user : result) {
//                            if (user.getUsername().equals(newusername)) {
                                View dialogView = getLayoutInflater().inflate(R.layout.fragment_third, null);
//                                break;
//                            }
//                        }
//                    }
//                    @Override
//                    public void onError(Exception e) {
//                        Log.e("Error fetching users", e.getMessage(), e);
//
//                    }
//                });
//
            }
        });
   }

    public void SetUpLER(View view) {//log in add the user to the list
        LER.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                View dialogView = getLayoutInflater().inflate(R.layout.activity_log_in, null);
                userRepository.getList(new UserRepository.UsersCallback<List<User>>() {
                    @Override
                    public void onSuccess(List<User> result) {
                        int id = List.getLast().getId();
                        id++;
                        EditText usernameET = dialogView.findViewById(R.id.inputUsername);
                        EditText passwordET = dialogView.findViewById(R.id.inputPassword);
                        String newusername = usernameET.toString();
                        String newpassword = passwordET.toString();
                        new User(List.getLast().getId(), newusername, newpassword);
                        User tempUser = new User(result.size() + 1, newusername, newpassword);
                        userRepository.AddUser(tempUser, new UserRepository.UsersCallback<User>() {
                            @Override
                            public void onSuccess(User result) {
                                View dialogView = getLayoutInflater().inflate(R.layout.fragment_third, null);
                            }
                            @Override
                            public void onError(Exception e) {
                                Log.e("Error fetching users", e.getMessage(), e);
                            }
                        });
                    }
                    @Override
                    public void onError(Exception e) {
                        Log.e("Error fetching users", e.getMessage(), e);
                    }
                });
            }
        });
    }
private static void createRoomXML(Room room) {
    String fileName = "room_" + room.getId() + ".xml";

    String xmlContent =
            "<Room>\n" +
                    "    <Id>" + room.getId() + "</Id>\n" +
                    "    <Difficulty>" + room.getDifficulty() + "</Difficulty>\n" +
                    "    <Rating>" + room.getRating() + "</Rating>\n" +
                    "</Room>";

    try (FileWriter writer = new FileWriter(fileName)) {
        writer.write(xmlContent);
    } catch (IOException e) {
        e.printStackTrace();
    }
    }

}




