package example;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import ev3dev.actuators.lego.motors.EV3LargeRegulatedMotor;
import ev3dev.actuators.lego.motors.EV3MediumRegulatedMotor;
import ev3dev.sensors.Battery;

import lejos.hardware.port.MotorPort;
import ev3dev.robotics.tts.Espeak;
import lejos.utility.Delay;

import static example.RunServer.*;

public class RunServer{

    public static boolean sync; //Flag for timer to activate

    public static ServerStarter serverStarter;//Server thread
    public static Timer timer;//Timer thread

    //Defined motors
    public static EV3LargeRegulatedMotor motorLeft;
    public static EV3LargeRegulatedMotor motorRight;
    public static EV3MediumRegulatedMotor latch;

    //Defined server socket and server class
    public static ServerSocket serverSocket;
    public static Server server;

    //Defined device battery
    public static Battery battery = Battery.getInstance();

    public static void main(String[] args) {

        //Instantiate motors
        motorLeft = new EV3LargeRegulatedMotor(MotorPort.A);
        motorRight = new EV3LargeRegulatedMotor(MotorPort.B);
        latch = new EV3MediumRegulatedMotor(MotorPort.C);

        //Instantiate server
        server = new Server();
        try {
            serverSocket = new ServerSocket(6666);//Create socket
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Create threads
        serverStarter = new ServerStarter();//server thread
        timer = new Timer();//timer thread

        //Start threads
        serverStarter.start();
        sync = false;//Set thread flag
        timer.start();
    }
}
//Server thread class
class ServerStarter extends Thread {

    public void run() {
        while (true) {
            RunServer.sync = true;//Set thread flag true
            try {
                server.start();//start a server
                server.stop();//stop the server(unused)
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}

//Server class
class Server{

    public Socket clientSocket;//Create client socket
    public static PrintWriter out;//Define server output buffer
    public static BufferedReader in;//Define server input buffer

    public static int currAngle;//Define variable for current angle of gate

    int max_speed = 1;//Define max speed for robot acceleration

    Boolean overwrite = false;//flag to overwrite acceleration

    //Set speed of latch
    public static void initMotorLatchSpeed(double latchSpeed){
        //convert rad/s to degrees/s
        latchSpeed= (latchSpeed*57.2957);
       // System.out.println("Defining motor speed to "+latchSpeed+" degrees/s\n");
        latch.setSpeed((int) latchSpeed);
    }

    //Method to move latch
    public static void moveLatch(int dir, double angToTurn){
        double relation=5.432; //relation between degrees to turn from angle in degrees and motor position
        int angg= (int) (angToTurn*relation);
        if(dir==0){ //open latch
            latch.rotate(angg);
        }else{ //close latch
            latch.rotate((-angg));
        }

    }


    public static void movement(double vel, double turnLeftMotorSpeed, double turnRightMotorSpeed, EV3LargeRegulatedMotor motorLeft, EV3LargeRegulatedMotor motorRight ){
        //begin to move

        double motorVelocity = 5 * vel * (int) Battery.getInstance().getVoltage();
        int leftSum = (int) (20 * turnLeftMotorSpeed * Battery.getInstance().getVoltage() + motorVelocity);
        int rightSum = (int) (20 * turnRightMotorSpeed * Battery.getInstance().getVoltage() + motorVelocity);


        if (leftSum < 0) {
            motorLeft.setSpeed(leftSum * (-1));
            motorLeft.backward();
        } else {
            motorLeft.setSpeed(leftSum);
            motorLeft.forward();
        }

        if (rightSum < 0) {
            motorRight.setSpeed(rightSum * (-1));
            motorRight.backward();
        } else {
            motorRight.setSpeed(rightSum);
            motorRight.forward();
        }

        // client.sendMessage("Got it");
    }

    public static void collect(double vel, double latchVelocity, EV3LargeRegulatedMotor motorLeft, EV3LargeRegulatedMotor motorRight){

        motorLeft.stop();
        motorRight.stop();
        latch.stop();
        initMotorLatchSpeed(latchVelocity);
        //åbne latch
        moveLatch(0, 55);
        //Delay.msDelay(500);
        //kørefrem
        movement(vel, 1,1,motorLeft, motorRight);
        Delay.msDelay(1500);
        motorLeft.stop();
        motorRight.stop();
        initMotorLatchSpeed(15);
        moveLatch(1, 55);
        latch.stop();
        //lukke latch

    }

    public static void drop( double latchVelocity, EV3LargeRegulatedMotor motorLeft, EV3LargeRegulatedMotor motorRight){

        motorLeft.stop();
        motorRight.stop();
        latch.stop();
        initMotorLatchSpeed(latchVelocity);
        moveLatch(0, 90);
        movement(10, 1,1,motorLeft, motorRight);
        Delay.msDelay(750);
        motorLeft.stop();
        motorRight.stop();
       /*
        int vel = 1;
        for (int i = 0; i < 5; i++){
            movement(vel, 1,1,motorLeft, motorRight);
            vel+=2;
        }
        Delay.msDelay(100);
        for (int i = 0; i < 5; i++){
            movement(vel, 1,1,motorLeft, motorRight);
            vel-=2;
        }*/


        movement(1, -1,-1,motorLeft, motorRight);
        Delay.msDelay(3000);
        motorLeft.stop();
        motorRight.stop();
        moveLatch(1, 90);
       /* movement(1, -1,1,motorLeft, motorRight);
        Delay.msDelay(4650);
        motorLeft.stop();
        motorRight.stop();
        movement(1, 1,1,motorLeft, motorRight);
        Delay.msDelay(1000);
        motorLeft.stop();
        motorRight.stop();*/
        //movement(20, -1,-1,motorLeft, motorRight);
        //moveLatch(1, 90);

        latch.stop();
        //ca. 30-36 cm afstand fra målet
    }

    public static void corner(double vel, double latchVelocity, EV3LargeRegulatedMotor motorLeft, EV3LargeRegulatedMotor motorRight){
        initMotorLatchSpeed(latchVelocity);
        //åbne latch
        moveLatch(0, 45);
        //Delay.msDelay(500);
        //kørefrem
        movement(0.8, 0,0,motorLeft, motorRight);
        Delay.msDelay(5000);
        motorLeft.stop();
        motorRight.stop();
        moveLatch(1, 45);
        latch.stop();
        movement(vel, -1,-1,motorLeft, motorRight);
        Delay.msDelay(1000);
        motorLeft.stop();
        motorRight.stop();
        //initMotorLatchSpeed(latchVelocity);
        //lukke latch

    }


    public void start() throws IOException {
            String response = "N/A";
            System.out.println("ready to recive");
            voice("what is my purpose");
            RunServer.sync = true;
            clientSocket = serverSocket.accept();
            RunServer.sync = true;
            voice("Connected");
            //voice("Connected. Battery level" + (int)(battery.getVoltage()*100) + " centivolt");
            System.out.println("er her");


            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            RunServer.sync = true;

            initMotorLatchSpeed(1);
            // latch.rotate(90);
            double vel = 0; //robots velocity
            double turnLeftMotorSpeed = 0; //left wheel speed rad/s
            double turnRightMotorSpeed = 0; //right wheel speed rad/s
            double latchVelocity = 1; //Latch velocity read from client
            double langle; //Latch angle
            int mangle; //Custom Latch angle(Maybe useless)


        //Robot runtime loop
            do {

                out.println("Got it");//ACK from server to client
                response = "N/A";//Initiate response

                //Read client command
                while (in.ready()) {
                    response = in.readLine();
                }

                //Semicolon spilts commands
                String[] commands = response.split(";");

                //Main commands
                for (String command : commands) {
                    String[] commandParts = command.replace(" ", "").split("-");
                    switch (commandParts[0]) {
                        case "turn":
                            boolean cc = false;
                            for (int i = 1; i < commandParts.length; i++) {

                                if (commandParts[i].charAt(0) == 'r') {
                                    cc = false;
                                    // response = client.sendMessage("Turning Right");
                                }
                                if (commandParts[i].charAt(0) == 'l') {
                                    cc = true;
                                    //response = client.sendMessage("Turning Left");
                                }

                                if (commandParts[i].charAt(0) == 's') {
                                    if (!cc) {
                                        turnLeftMotorSpeed = Double.parseDouble(commandParts[i].substring(1));
                                        turnRightMotorSpeed = Double.parseDouble(commandParts[i].substring(1)) * -1;
                                        // response = client.sendMessage("Turn right");
                                    } else {
                                        turnLeftMotorSpeed = Double.parseDouble(commandParts[i].substring(1)) * -1;
                                        turnRightMotorSpeed = Double.parseDouble(commandParts[i].substring(1));
                                        //  response = client.sendMessage("turn left");
                                    }
                                }
                            }
                            break;

                        case "drive":
                            for (int i = 1; i < commandParts.length; i++) {
                                if (commandParts[i].charAt(0) == 's') {
                                    vel = Double.parseDouble(commandParts[i].substring(1));
                                    if (max_speed < vel && !overwrite){
                                        vel = max_speed;
                                        max_speed++;
                                    }
                                    //  response = client.sendMessage("drive");
                                }

                                if (commandParts[i].charAt(0) == 'b') {
                                    turnRightMotorSpeed = Double.parseDouble(commandParts[i].substring(1)) * -1;
                                    turnLeftMotorSpeed = Double.parseDouble(commandParts[i].substring(1)) * -1;
                                    //  response = client.sendMessage("Back");
                                }

                                if (commandParts[i].charAt(0) == 'o') {
                                    overwrite = true;
                                }

                            }
                            break;

                        case "collect":

                            for (int i = 1; i < commandParts.length; i++) {
                                if (commandParts[i].charAt(0) == 's') {
                                    vel = Double.parseDouble(commandParts[i].substring(1)); //drive speed

                                }

                                if (commandParts[i].charAt(0) == 'g') {
                                    latchVelocity = Double.parseDouble(commandParts[i].substring(1));
                                }

                            }
                            collect(vel, latchVelocity, motorLeft, motorRight);
                            vel = 0;
                            break;

                        case "gate":
                            for (int i = 1; i < commandParts.length; i++) {
                                if (commandParts[i].charAt(0) == 'u') { //move latch up
                                    langle = Double.parseDouble(commandParts[i].substring(1));
                                    moveLatch(0, langle);
                                }

                                if (commandParts[i].charAt(0) == 'd') { //move latch down
                                    langle = Double.parseDouble(commandParts[i].substring(1));
                                    moveLatch(1, langle);
                                }

                                if (commandParts[i].charAt(0) == 'a') { //move latch to custom angle
                                    langle = Double.parseDouble(commandParts[i].substring(1));
                                    mangle = (int) (langle - currAngle);
                                    latch.rotate(mangle);
                                }

                                if (commandParts[i].charAt(0) == 's') { //drive speed
                                    vel = Double.parseDouble(commandParts[i].substring(1));

                                }

                                if (commandParts[i].charAt(0) == 'g') { //gate speed
                                    latchVelocity = Double.parseDouble(commandParts[i].substring(1));
                                }

                            }
                            break;


                        case "drop":
                            drop(4, motorLeft, motorRight);
                            vel = 0;
                            break;

                        case "corner"://corner and wall collect: 15 cm with 0.5cm error margin
                            corner(2,3, motorLeft, motorRight);
                            vel = 0;
                            break;

                        case "battery"://corner and wall collect: 15 cm with 0.5cm error margin
                            voice("Battery level" + (int)(battery.getVoltage()*100) + " centivolt");
                            break;

                        case "stop":
                            for (int i = 1; i < commandParts.length; i++) {
                                // "stop -d" stop driving
                                if (commandParts[i].charAt(0) == 'd') {
                                    //motorLeft.stop();
                                    //motorRight.stop();

                                    vel = 0;
                                    max_speed = 1;
                                    overwrite = false;
                                    //response = client.sendMessage("stop drive");
                                }
                                // "stop -t" stop turning
                                if (commandParts[i].charAt(0) == 't') {
                                    turnLeftMotorSpeed = 0;
                                    turnRightMotorSpeed = 0;
                                    // response = client.sendMessage("stop turn");
                                }
                                // "stop -g" stop gate
                                if (commandParts[i].charAt(0) == 'g') {
                                    initMotorLatchSpeed(0);
                                }
                            }
                            break;
                        case "timer":
                            for (int i = 1; i < commandParts.length; i++) {
                                if (commandParts[i].charAt(0) == 't') {//Timer set custom seconds
                                    Timer.MAX_COUNT = Integer.parseInt(commandParts[i].substring(1));
                                    voice("Timer set to" + Timer.MAX_COUNT + "seconds");
                                }

                                if (commandParts[i].charAt(0) == 'r') {//Timer reset to on minute
                                    Timer.MAX_COUNT = 60;
                                    voice("Timer reset");
                                }

                                if (commandParts[i].charAt(0) == 'm') {//Timer set to one minute
                                    Timer.MAX_COUNT = 60;
                                    voice("Timer set to one minute");
                                }

                                if (commandParts[i].charAt(0) == 'h') {//Timer set to one hour
                                    Timer.MAX_COUNT = 360000;
                                    voice("Timer set to one hour");
                                }
                               // voice("Timer set to" + Timer.MAX_COUNT + "seconds");

                            }
                            break;
                        default:
                            break;

                    }
                }
                //Insert movement values given from client into robot movement
                movement(vel, turnLeftMotorSpeed, turnRightMotorSpeed, motorLeft, motorRight);

                //Test if client still respond
                if (!response.equals("N/A")) {
                    System.out.println(response);
                    RunServer.sync = true;
                }
               /* if(Timer.counter == 10){
                    motorLeft.stop();
                    motorRight.stop();
                    latch.stop();
                }*/

            } while (!response.equals("exit"));
                 voice("Goodbye world");//exit response
                 Timer.counter = Timer.MAX_COUNT;//Timer thread closes server thread and opens new.

                 while (true){
                    //Just giving thread time to close
                 }
        }

    //Close down server method
    public void stop() throws IOException {
        if(in != null)
        in.close();
        if(out != null)
        out.close();
        if(clientSocket != null)
        clientSocket.close();
        if(serverSocket != null)
        serverSocket.close();
    }

    //TTS method
    public static void voice(String arg){
        Espeak TTS = new Espeak();
        TTS.setVoice("en");
        TTS.setSpeedReading(105);
        TTS.setPitch(60);
        TTS.setMessage(arg);
        TTS.say();
    }

}

//Timer thread
class Timer extends Thread{

    public static int counter;//Timeout counter
    public static int MAX_COUNT = 60;//Amount of seconds before timeout

    public void run(){
        counter = 0;//Start count at zero

        do {
            if (RunServer.sync) {//If thread flag is true client is still running
                counter = 0;//Reset count
                RunServer.sync = false;//Reset flag
            }

            try {
                Thread.sleep(1000);//One sec delay to match count with seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            counter++;//Increase timer count

            if(counter > 10){
                motorLeft.stop();
                motorRight.stop();
                latch.stop();
            }

            if(counter > MAX_COUNT){//If timer reaches max count

                //Stop robot motors
                motorLeft.stop();
                motorRight.stop();
                latch.stop();

                try {
                    server.stop();//Stop server
                } catch (IOException e) {
                    e.printStackTrace();
                }
                RunServer.serverStarter.stop();//Kill server thread
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                server = new Server();//Start new server
                try {
                    serverSocket = new ServerSocket(6666);//Open new server port
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                RunServer.serverStarter = new ServerStarter();//Create new server thread
                RunServer.serverStarter.start();//Start server thread
                counter = 0;//Reset count
            }
        } while (true);
    }
}