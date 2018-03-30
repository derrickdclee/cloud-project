package edu.duke.compsci290.partyappandroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class PartyModesActivity extends AppCompatActivity {
    private Button mHostButton;
    private Button mBouncerButton;
    private Button mInviteeButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_modes);
        mHostButton = findViewById(R.id.host_mode_button);
        mBouncerButton = findViewById(R.id.bouncer_mode_button);
        mInviteeButton = findViewById(R.id.invitee_mode_button);
        mHostButton.setText("Host Mode");
        mHostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToHostMode();
            }
        });
        mBouncerButton.setText("Bouncer Mode");
        mInviteeButton.setText("Invitee Mode");
    }
    private void goToHostMode(){
        Intent intent = new Intent(this, HostActivity.class);
        this.startActivity(intent);
    }
}
