package com.example.escape_rooms;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomePage extends Fragment {
    private final UserRepository userRepository;

    public HomePage() {
        userRepository = new UserRepository();
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
        SetUpLoginEscapeRoom(view);
        SetUpCR(view);
    }
    private Button MyEscapeRoom = null;
    private Button CER = null;
    private Button CR = null;
    private Button SER = null;
    private Button LER = null;
    private ArrayList<User> List = null;

    User user = null;
    private Room[] rooms = {
            new Room("Easy", 1, 5),
            new Room("Medium", 2, 7),
            new Room("Hard", 3, 9)
    };
    private void setUpRecyclerView(View view){
        assert getView() != null;
        MyEscapeRoom = getView().findViewById(R.id.MyER);
        CER = getView().findViewById(R.id.CER);
        CR = getView().findViewById(R.id.CR);
        SER = getView().findViewById(R.id.SER);
        LER = getView().findViewById(R.id.LER);
    }
    public void SetUpMyEscapeRoom(View view) {//my levels
        MyEscapeRoom.setOnClickListener(new View.OnClickListener() {
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
                View PictureForObject =getView().findViewById(R.id.PictureForObject);
                View Backround=getView().findViewById(R.id.Backround);
                for(Room room :rooms) {
                    View buttoninsidePictureForObject =getView().findViewById(R.id.buttoninsidePictureForObject);
                    buttoninsidePictureForObject.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view){


                        }});
//                     //View name= getLayoutInflater().inflate(R.layout."room_" + room.getId() + ".xml", null);
               }

            }
        });
    }

    public void SetUpSER(View view) {//sign up, see if the user is in the list and move him to the next page
        SER.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View dialogView = getLayoutInflater().inflate(R.layout.sign_in, null);
                new AlertDialog.Builder(getContext())
                        .setView(dialogView)
                        .setPositiveButton("Sign In", (dialog, which) -> {
                            userRepository.getAllUsers(new UserRepository.UsersCallback<List<User>>() {
                                @Override
                                public void onSuccess(List<User> result) {
                                int id = result.get(result.size()-1).getId();
                                id++;
                                EditText usernameET = dialogView.findViewById(R.id.ETusername);
                                EditText passwordET = dialogView.findViewById(R.id.ETpassword);
                                String newusername = usernameET.getText().toString();
                                String newpassword = passwordET.getText().toString();
                                User tempUser = new User(result.size() + 1, newusername, newpassword);
                                userRepository.addUser(tempUser, new UserRepository.UsersCallback<User>() {
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
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
   }

    public void SetUpLoginEscapeRoom(View view) {//log in add the user to the list
        LER.setOnClickListener(view1 -> {
            View dialogView = getLayoutInflater().inflate(R.layout.activity_log_in, null);
            new AlertDialog.Builder(getContext())
                    .setView(dialogView)
                    .setPositiveButton("Login", (dialog, which) -> {
                        userRepository.getAllUsers(new UserRepository.UsersCallback<List<User>>() {
                            @Override
                            public void onSuccess(List<User> result) {
                                    EditText usernameET = dialogView.findViewById(R.id.inputUsername);
                                    EditText passwordET = dialogView.findViewById(R.id.inputPassword);
                                    String newusername = usernameET.getText().toString();
                                    String newpassword = passwordET.getText().toString();
                                    for (User user : result) {
                                        if (user.getUsername().equals(newusername)) {
                                            View dialogView = getLayoutInflater().inflate(R.layout.fragment_third, null);
                                            break;
                                        }
                                    }


                            }
                            @Override
                            public void onError(Exception e) {
                                Log.e("Error fetching users", e.getMessage(), e);
                            }
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
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





