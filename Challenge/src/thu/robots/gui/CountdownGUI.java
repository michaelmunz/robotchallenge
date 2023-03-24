package thu.robots.gui;

import javax.swing.*;

public class CountdownGUI extends JFrame {
    private JPanel panel;
    private JLabel lTime;
    private JLabel lText;


    public CountdownGUI(){
        setUndecorated(true);
        setResizable(false);
        setExtendedState(MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setOpacity(0.5f);
        setVisible(false);
        setContentPane(panel);

    }

    public void setString(String text){
        this.lText.setText(text);
    }
    
    public void updateTime(int currentTimeSeconds) {
        lTime.setText(""+currentTimeSeconds);
    }
}
